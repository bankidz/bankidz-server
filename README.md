# BANKIDZ-SERVER


![image](https://user-images.githubusercontent.com/63996052/180037052-29f57dd5-ef81-4062-8326-472c7c2b27be.png)


<div align="center"> 
💰 실전 금융 경험을 통해 어린이 금융 리터러시를 향상시키다, <b>BANKIDZ</b> 🐷
</div>


---

## 🍭 Main Service

**돈길 생성**: 자신의 목표를 기반으로 저축 계획과 방법(이자율, 절반보상, 형제와 경쟁 등)을 생성할 수 있습니다.

**돈길 수락/거절**: 부모는 자녀가 생성한 돈길에 대하여 한 줄 코멘트와 함께 수락 및 거절을 할 수 있습니다.

**마이페이지**: 돈길 관련 통합 데이터(총 돈길, 성공률 등)를 확인할 수 있으며, 가족 코드 공유를 통해 가족을 초대할 수 있습니다.

<!--
---

## 🎠 WorkFlow

-->
---

## 💻 Back-End Developers

<div align="center"> 

| <img width=200px src="https://user-images.githubusercontent.com/63996052/180594209-46c7eec1-f08d-41cc-842d-0f575e794b65.png"/> | <img width=200px src="https://user-images.githubusercontent.com/63996052/180594175-23892209-1c7b-4862-b448-08e090a2a139.png"/> |
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

## 🏛 Architecture

![KakaoTalk_Photo_2022-07-11-20-35-48](https://user-images.githubusercontent.com/59060780/178255707-814eb2ac-be3a-4350-940c-f060890c2420.jpeg)

---

## 🌈 Test Coverage

![image](https://user-images.githubusercontent.com/63996052/180591843-3344c378-bd7f-4487-bc08-ce44e0da3fb9.png)

---

###### 2022 Bankidz Server
