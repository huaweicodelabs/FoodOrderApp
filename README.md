# Harmony POS FoodOrder Application

# HarmonyOS
Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.

## Table of Contents
* [Introduction](#introduction)
* [Installation](#installation)
* [Supported Environments](#supported-environments)
* [Configuration](#configuration)
* [Sample Code](#sample-code)
* [License](#license)

## Introduction
The HarmonyOS POS Food Order app greatly simplifies the ordering process for both the customer and the restaurant. 

This app presents an interactive and up-to-date menu with all available options in an easy to use manner. 

With this app, customers can:
●	Choose one or more items to place an order which will land in the cart.
●	View all the order details in the cart before checking out.
●	Get the order confirmation details.

To develop the HarmonyOS POS Food Order app, you need to:
●	Register a HUAWEI ID on HUAWEI Developers. Then, create an app, configure app information in AppGallery Connect.
●	Create a HarmonyOS project in DevEco Studio.


## Installation

To use functions provided by examples, please make sure Harmony OS (2.0 or later) and HMS Core (APK) version 6.1.0.300 or later has been installed on your cellphone.
There are two ways to install the sample demo:

* You can compile and build the codes in DevEco Studio. After building the .hap, you can install it on the phone/wearable watch and debug it.
* Generate the .hap file from Gradle. Use the ADB tool to install the .hap on the phone/wearable watch and debug it adb install
{YourPath}\phone\build\outputs\hap\debug\phone-debug-rich-signed.hap
{YourPath}\smartwatch\build\outputs\hap\debug\smartwatch-debug-rich-signed.hap

## Supported Environments

Harmony SDK Version >= 5 and JDK version >= 1.8 is recommended.

## Configuration

Create an app in AppGallery Connect.
In DevEco Studio, Configuring App Signature Information, Generating a Signing Certificate Fingerprint and Configuring the Signing Certificate Fingerprint in App Gallery.


## Sample Code

The Harmony POS FoodOrder App Demo provides demonstration for following scenarios:

●	How to declare distributed data management permissions.
●	How to get the online devices in a distributed network.
●	How to connect to a Service Ability.
●	How to remotely control device B from device A.


* [Distributed capablities](https://developer.harmonyos.com/en/docs/documentation/doc-guides/harmonyos-features-0000000000011907#section417848172013)


## License
Harmony Learning Application is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).