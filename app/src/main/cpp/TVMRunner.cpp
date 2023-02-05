#include "TVMRunner.h"

#include <tvm/runtime/packed_func.h>
#include <tvm/runtime/registry.h>

#include <chrono>

namespace {
    const std::string device_type = "CPU";
    const struct {
        const int b = 1;
        const int h = 28;
        const int w = 28;
        const int c = 1;
    } DIMS;
    const int OUT_DIM = 10;
}

using Timestamp = std::chrono::time_point<std::chrono::high_resolution_clock>;

void TVMRunner::setModelPathAndInit(const char* fileName) {
    m_dev = {kDLCPU, 0};
    if (device_type == "OpenCL") {
        m_dev = {kDLOpenCL, 0};
    }

    std::string name(fileName);
    tvm::runtime::Module mod_factory = tvm::runtime::Module::LoadFromFile(name);
    m_module = mod_factory.GetFunction("default")(m_dev);
    m_setInputFunc = m_module.GetFunction("set_input");
    m_getOutputFunc = m_module.GetFunction("get_output");
    m_runFunc = m_module.GetFunction("run");
}

std::string TVMRunner::run(const std::vector<int>& input) {
    DLDevice devCPU{kDLCPU, 0};
    tvm::runtime::NDArray x = tvm::runtime::NDArray::Empty({DIMS.b, DIMS.h, DIMS.w, DIMS.c}, DLDataType{kDLFloat, 32, 1}, devCPU);
    for (size_t i = 0; i < input.size(); ++i) {
        static_cast<float*>(x->data)[i] = input[i];
    }

    m_setInputFunc(0, x);
    TVMSynchronize(m_dev.device_type, m_dev.device_id, nullptr);
    Timestamp start = std::chrono::high_resolution_clock::now();
    m_runFunc();
    TVMSynchronize(m_dev.device_type, m_dev.device_id, nullptr);
    Timestamp end = std::chrono::high_resolution_clock::now();
    double duration = std::chrono::duration_cast<std::chrono::nanoseconds>(end - start).count() * 1e-6;
    m_inferenceTime = std::to_string(duration);
    m_inferenceTime += " ms";

    tvm::runtime::NDArray probCpu = tvm::runtime::NDArray::Empty({1, OUT_DIM}, DLDataType{kDLFloat, 32, 1}, devCPU);
    tvm::runtime::NDArray probGpu = m_getOutputFunc(0);
    probCpu.CopyFrom(probGpu);

    const float* oData = static_cast<float*>(probCpu->data);
    int maxIndex = 0;
    for (size_t j = 1; j < OUT_DIM; j++) {
        if (oData[maxIndex] < oData[j]) {
            maxIndex = j;
        }
    }

    return std::to_string(maxIndex);
}