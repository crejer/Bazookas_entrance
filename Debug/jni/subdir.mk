################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/DetectionBasedTracker_jni.cpp 

OBJS += \
./jni/DetectionBasedTracker_jni.o 

CPP_DEPS += \
./jni/DetectionBasedTracker_jni.d 


# Each subdirectory must supply rules for building sources it contributes
jni/%.o: ../jni/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -I/Users/jeroen/Documents/development/android-ndk-r10c/platforms/android-9/arch-arm/usr/include -I/Users/jeroen/Documents/development/android-ndk-r10c/sources/cxx-stl/gnu-libstdc++/4.6/include -I/Users/jeroen/Documents/development/android-ndk-r10c/sources/cxx-stl/gnu-libstdc++/4.6/libs/armeabi-v7a/include -I/Users/jeroen/Documents/development/OpenCV-2.4.9-android-sdk/sdk/native/jni/include -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


