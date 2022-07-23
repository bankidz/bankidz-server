# BANKIDZ-SERVER


![image](https://user-images.githubusercontent.com/63996052/180037052-29f57dd5-ef81-4062-8326-472c7c2b27be.png)


<div align="center"> 
💰 실전 금융 경험을 통해 어린이 금융 리터러시를 향상시키다, <b>BANKIDZ</b> 🐷
</div>

<!--
---

## 🍭 Main Service


---

## 🎠 WorkFlow

-->
---

## 💻 Back-end Developers

<div align="center"> 

|  |  |
| :----------------------------------------------------------: | :----------------------------------------------------------: | 
|                          주어진사랑                          |                          김민준                               |
|             [ozzing](https://github.com/ozzing)              |          [sanbonai06](https://github.com/sanbonai06)         |
</div>

   


---

## 〽 Git Branch Strategy

![image](https://user-images.githubusercontent.com/63996052/180049335-1c101dd3-6d1c-415b-85c3-e3264a28c797.png)

---

## 📑 Convention

![image](https://user-images.githubusercontent.com/63996052/180043284-95b8e582-a270-4b59-8528-2bd9c8011d2a.png)
![image](https://user-images.githubusercontent.com/63996052/180049517-c1b506ac-9c37-4ee0-9375-0f9c07355637.png)

---

## 📁 Project Foldering

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
│─main
│   |─resources
│   └─java/com.ceos.bankids
│       │    
│       │────config
│       │      └─security
│       │    
│       │────controller
│       │      └─request
│       │      
│       │────domain
│       │      
│       │────dto
│       │      └─oauth
│       │      
│       │────exception
│       │     
│       │────repository
│       │
│       │────service
│       │
│       └─BankidsApplication
│
└─test
    └─java/com.ceos.bankids/unit
        │   
        │────controller
        │   
        │────service
        │
        └─BankidsApplicationTests
```

---

## 🛠️ Dependencies

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

## 🚧 ERD

![image](https://user-images.githubusercontent.com/63996052/180050308-788f7b04-e599-48d0-b039-7d7d3cbe2b7f.png)

---
###### 2022 Bankidz Server
