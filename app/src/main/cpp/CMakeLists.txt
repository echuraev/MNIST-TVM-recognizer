cmake_minimum_required(VERSION 3.22.1)
project("mnist_tvm_recognizer")
set(CMAKE_CXX_STANDARD 17)

find_library(
    log-lib
    log
)

add_library(tvm_runtime SHARED IMPORTED) # or STATIC instead of SHARED
set_target_properties(tvm_runtime PROPERTIES
        IMPORTED_LOCATION
        "${CMAKE_SOURCE_DIR}/jniLibs/${CMAKE_ANDROID_ARCH_ABI}/libtvm_runtime.so"
        )
add_library(cpp_lib SHARED IMPORTED) # or STATIC instead of SHARED
set_target_properties(cpp_lib PROPERTIES
        IMPORTED_LOCATION
        "${CMAKE_SOURCE_DIR}/jniLibs/${CMAKE_ANDROID_ARCH_ABI}/libc++_shared.so"
        )

set (TVM_HOME $ENV{TVM_HOME})
include_directories(
        ${CMAKE_SOURCE_DIR}
        ${TVM_HOME}/3rdparty/dlpack/include
        ${TVM_HOME}/3rdparty/dmlc-core/include
        ${TVM_HOME}/include
)

add_library(${PROJECT_NAME} SHARED
        mnist_runner.cpp
        TVMRunner.cpp
        )

target_link_libraries(${PROJECT_NAME}
    ${log-lib}
    tvm_runtime
    cpp_lib
)