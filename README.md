# Kotlin Auto Fill
![version badge](https://img.shields.io/badge/version-1.0.6-informational)

<!-- Plugin description start -->
the most simple and powerful intellij plugin of parameter helper

Installing right now if you kotlin users.     
maybe you will be enchanted 🧙🏽‍♂️  

- only class constructor, functions arguments named
- fill with **default values**
- fill with **random values** 

<a target="_blank" href=https://user-images.githubusercontent.com/38849685/207317959-eb2f5d4d-7bdc-4560-bfdb-5763236d9b9c.gif>
<img style="margin-bottom:20px;" width="700" src="https://user-images.githubusercontent.com/38849685/207317959-eb2f5d4d-7bdc-4560-bfdb-5763236d9b9c.gif" alt="auto-fill-kotlin tutorial"/></a>

<br>  

### Configuration   

You can change configuration in Intellij Inspection Settings.  

<a target="_blank" href="https://user-images.githubusercontent.com/38849685/207307931-a826bbed-39a9-4896-b703-d4297a3720c9.png">
 <img width="500" src="https://user-images.githubusercontent.com/38849685/207307931-a826bbed-39a9-4896-b703-d4297a3720c9.png" alt="auto-fill-kotlin tutorial">
</a>  

<!-- Plugin description end -->

## Change Log
- 0.0.1-M1
  - First deploy Kotlin Auto Fill with basic features
  - support up to Intellij IDE version 2022.3

- 1.0.0
  - updated random value quickFix
  - updated only name arguments quickFix
  - refactor configuration variable structure
  - fix some bugs

- 1.0.1
  - support since to Intellij IDE version 2021.1 
  - delete lambda function generator
    - It was a feature that wasn't as helpful as I thought. Version compatibility of the APIs used in the proposed parameter name algorithm was not flexible. and another platform versions support was being delayed because of this feature.

- 1.0.2
  - support up to Intellij IDE version 231.*
  - Fixed a bug with boolean values being converted to strings when using "random generator"

- 1.0.3
  - support up to Intellij IDE version 232.*

- 1.0.4
  - support up to Intellij IDE version 233.*

- 1.0.5
  - support up to Intellij IDE version 241.*

- 1.0.6 (2024.11.18)
  - support up to Intellij IDE version 242.*