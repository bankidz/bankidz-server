# BANKIDZ-SERVER


![image](https://user-images.githubusercontent.com/63996052/180037052-29f57dd5-ef81-4062-8326-472c7c2b27be.png)


<div align="center"> 
π° μ€μ  κΈμ΅ κ²½νμ ν΅ν΄ μ΄λ¦°μ΄ κΈμ΅ λ¦¬ν°λ¬μλ₯Ό ν₯μμν€λ€, <b>BANKIDZ</b> π·
</div>


---

## π­ Main Service

**λκΈΈ μμ±**: μμ μ λͺ©νλ₯Ό κΈ°λ°μΌλ‘ μ μΆ κ³νκ³Ό λ°©λ²(μ΄μμ¨, μ λ°λ³΄μ, νμ μ κ²½μ λ±)μ μμ±ν  μ μμ΅λλ€.

**λκΈΈ μλ½/κ±°μ **: λΆλͺ¨λ μλκ° μμ±ν λκΈΈμ λνμ¬ ν μ€ μ½λ©νΈμ ν¨κ» μλ½ λ° κ±°μ μ ν  μ μμ΅λλ€.

**λ§μ΄νμ΄μ§**: λκΈΈ κ΄λ ¨ ν΅ν© λ°μ΄ν°(μ΄ λκΈΈ, μ±κ³΅λ₯  λ±)λ₯Ό νμΈν  μ μμΌλ©°, κ°μ‘± μ½λ κ³΅μ λ₯Ό ν΅ν΄ κ°μ‘±μ μ΄λν  μ μμ΅λλ€.

<!--
---

## π  WorkFlow

-->
---

## π» Back-End Developers

<div align="center"> 

| <img width=200px src="https://user-images.githubusercontent.com/63996052/180594209-46c7eec1-f08d-41cc-842d-0f575e794b65.png"/> | <img width=200px src="https://user-images.githubusercontent.com/63996052/180594175-23892209-1c7b-4862-b448-08e090a2a139.png"/> |
| :----------------------------------------------------------: | :----------------------------------------------------------: | 
|                          μ£Όμ΄μ§μ¬λ                          |                          κΉλ―Όμ€                               |
|             [ozzing](https://github.com/ozzing)              |          [sanbonai06](https://github.com/sanbonai06)         |
</div>

   


---

## γ½ Git Branch Strategy

![image](https://user-images.githubusercontent.com/63996052/180049335-1c101dd3-6d1c-415b-85c3-e3264a28c797.png)

---

## π Convention

![image](https://user-images.githubusercontent.com/63996052/180043284-95b8e582-a270-4b59-8528-2bd9c8011d2a.png)
![image](https://user-images.githubusercontent.com/63996052/180049517-c1b506ac-9c37-4ee0-9375-0f9c07355637.png)

---

## π Project Foldering

```
.idea
config
gradle
.gitignore
build.gradle
docker-compose.yml
Dockerfile
Dockerfile.prod
gradlew
gradlew.bat
HELP.md
LICENSE
READEME.md
settings.gradle
src
ββmain
β   |βresources
β   ββjava/com.ceos.bankids
β       β    
β       βββββconfig
β       β      ββsecurity
β       β    
β       βββββcontroller
β       β      ββrequest
β       β      
β       βββββdomain
β       β      
β       βββββdto
β       β      ββoauth
β       β      
β       βββββexception
β       β     
β       βββββrepository
β       β
β       βββββservice
β       β
β       ββBankidsApplication
β
ββtest
    ββjava/com.ceos.bankids/unit
        β   
        βββββcontroller
        β   
        βββββservice
        β
        ββBankidsApplicationTests
```

---

## π οΈ Dependencies

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-data-jdbc'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-webflux:2.7.0'
    implementation group: 'org.json', name: 'json', version: '20220320'
    implementation 'io.jsonwebtoken:jjwt:0.9.1'
    implementation 'io.springfox:springfox-boot-starter:3.0.0'
    implementation 'io.springfox:springfox-swagger-ui:3.0.0'
    implementation 'com.github.maricn:logback-slack-appender:1.4.0'
    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'mysql:mysql-connector-java'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.8.2'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.8.2'
    testImplementation 'org.mockito:mockito-core:4.5.1'
    testImplementation 'org.mockito:mockito-junit-jupiter:4.5.1'
    testImplementation 'org.assertj:assertj-core:3.22.0'
    testImplementation 'net.bytebuddy:byte-buddy:1.12.9'
}
```
---

## π§ ERD

![image](https://user-images.githubusercontent.com/63996052/180050308-788f7b04-e599-48d0-b039-7d7d3cbe2b7f.png)

---

## π Architecture

![KakaoTalk_Photo_2022-07-11-20-35-48](https://user-images.githubusercontent.com/59060780/178255707-814eb2ac-be3a-4350-940c-f060890c2420.jpeg)

---

## π Test Coverage

![image](https://user-images.githubusercontent.com/63996052/180591843-3344c378-bd7f-4487-bc08-ce44e0da3fb9.png)

---

###### 2022 Bankidz Server
