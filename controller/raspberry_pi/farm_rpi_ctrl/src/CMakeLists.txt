cmake_minimum_required(VERSION 2.8.0)

set(BINNAME farms_rpi_ctrl)
project(${BINNAME})

set(CMAKE_CXX_FLAGS "-std=c++11")

if (${GOAL} STREQUAL "pi")
    include (CMakeForceCompiler)
    CMAKE_FORCE_CXX_COMPILER(arm-linux-gnueabihf-g++ GNU)
    CMAKE_FORCE_C_COMPILER(arm-linux-gnueabihf-gcc GNU) 
    add_definitions( -DRASPBERRY )
else()
endif()

if(NOT TOP_DIR)
    set(TOP_DIR ${CMAKE_CURRENT_SOURCE_DIR}/..)
endif()

if(NOT OUT_DIR)
    set(OUT_DIR ${TOP_DIR})
endif()

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${OUT_DIR}/bin)
MESSAGE(${CMAKE_RUNTIME_OUTPUT_DIRECTORY})

set(SRC
    ${CMAKE_CURRENT_SOURCE_DIR}/uart.cpp
    ${CMAKE_CURRENT_SOURCE_DIR}/farm_rpi_ctrl.cpp)

set(INCLUDE_DIR
     ${CMAKE_CURRENT_SOURCE_DIR})
             
include_directories(
    ${INCLUDE_DIR})
    
set(HEADERS
    ${INCLUDE_DIR}/uart.h)

        
add_executable(${BINNAME} ${SRC} ${HEADERS})