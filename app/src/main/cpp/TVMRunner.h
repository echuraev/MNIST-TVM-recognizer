#ifndef MNIST_TVM_RECOGNIZER_TVMRUNNER_H
#define MNIST_TVM_RECOGNIZER_TVMRUNNER_H

#include <string>
#include <vector>

#include <tvm/runtime/module.h>

class TVMRunner {
public:
    static TVMRunner& getInstance() {
        static TVMRunner instance;
        return instance;
    }

    void setModelPathAndInit(const char* fileName);
    std::string run(const std::vector<int>& input);
    inline std::string getInferenceTime() { return m_inferenceTime; }

private:
    TVMRunner() = default;
    ~TVMRunner() = default;
    TVMRunner(const TVMRunner&) = delete;
    TVMRunner& operator=(const TVMRunner&) = delete;

    DLDevice m_dev;
    tvm::runtime::Module m_module;
    tvm::runtime::PackedFunc m_setInputFunc;
    tvm::runtime::PackedFunc m_getOutputFunc;
    tvm::runtime::PackedFunc m_runFunc;
    std::string m_inferenceTime;
};


#endif //MNIST_TVM_RECOGNIZER_TVMRUNNER_H
