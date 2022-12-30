# BANKIDZ-SERVER



<div align="center"> 

💰 실전 금융 경험을 통해 어린이 금융 리터러시를 향상시키다, <b>BANKIDZ</b> 🐷
<br>

![image](https://user-images.githubusercontent.com/63996052/195283994-7e35ab95-dd6a-411f-bea0-dc5d7da35170.png)

|  AD  | Instagram |                 Web                 |                           iOS App                            |                         Android App                          |
| :--: | :-------: | :---------------------------------: | :----------------------------------------------------------: | :----------------------------------------------------------: |
|  -   |     -     | [bankidz.com](https://bankidz.com/) | <a href="https://apps.apple.com/app/%EB%B1%85%ED%82%A4%EC%A6%88/id6444064518"><img src="https://tools.applemediaservices.com/api/badges/download-on-the-app-store/black/ko-kr?size=250x83&amp;releaseDate=1654300800&h=dd4ccd7fb22c609cf9132f37bf23c390" alt="Download on the App Store" style="border-radius: 13px; width: 250px; height: 83px;"></a> | <a href='https://play.google.com/store/apps/details?id=com.bankidz.bankidzapp'><img alt='다운로드하기 Google Play' width='285px' src='https://play.google.com/intl/en_us/badges/static/images/badges/ko_badge_web_generic.png'/></a> |
</div>


* 뱅키즈는 웹뷰 환경에 최적화 되어 있습니다. 기타 웹 브라우저 환경에서는 일부 기능이 작동하지 않습니다.
* 뱅키즈 앱은 현재 App Store, Google Play 출시를 위해 심사중입니다.


## Main Feature
#### 자녀
![image](https://user-images.githubusercontent.com/63996052/195284727-16bcb0a5-d8e4-4cb2-9610-bdab1ee28369.png)
![image](https://user-images.githubusercontent.com/63996052/195285361-22d6a507-e426-41cb-8390-e17507e2878e.png)
#### 부모
![image](https://user-images.githubusercontent.com/63996052/195285132-86c4d1f5-a201-4259-b575-c15d66694430.png)
![image](https://user-images.githubusercontent.com/63996052/195285410-12eb2e3a-7356-49c7-b8f1-cd7d7aacfc9c.png)


## WorkFlow

![image](https://user-images.githubusercontent.com/63996052/195284780-45cff104-98ba-475e-b90f-158d4f503fec.png)

---

## Back-End Developers

<div align="center"> 

| <img width=200px src="https://user-images.githubusercontent.com/63996052/180594209-46c7eec1-f08d-41cc-842d-0f575e794b65.png"/> | <img width=200px src="https://user-images.githubusercontent.com/63996052/180594175-23892209-1c7b-4862-b448-08e090a2a139.png"/> |
| :----------------------------------------------------------: | :----------------------------------------------------------: | 
|                          주어진사랑                          |                          김민준                               |
|             [ozzing](https://github.com/ozzing)              |          [sanbonai06](https://github.com/sanbonai06)         |
</div>

## Skills
<img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/spring%20boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/junit5-25A162?style=for-the-badge&logo=junit5&logoColor=white"> <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">

<img src="https://img.shields.io/badge/ec2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white"> <img src="https://img.shields.io/badge/rds-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white"> <img src="https://img.shields.io/badge/swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white"> <img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/github%20actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white">

## Git Branch Strategy

![image](https://user-images.githubusercontent.com/63996052/180049335-1c101dd3-6d1c-415b-85c3-e3264a28c797.png)


## Convention

![image](https://user-images.githubusercontent.com/63996052/180043284-95b8e582-a270-4b59-8528-2bd9c8011d2a.png)
![image](https://user-images.githubusercontent.com/63996052/180049517-c1b506ac-9c37-4ee0-9375-0f9c07355637.png)


## Project Foldering

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
│       │────constant
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
│       │────mapper
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

## Dependencies

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
    implementation 'org.springframework.cloud:spring-cloud-starter-aws:2.0.1.RELEASE'
    implementation 'com.nimbusds:nimbus-jose-jwt:9.24.2'
    implementation 'org.bouncycastle:bcprov-ext-jdk15to18:1.64'
    implementation group: 'com.google.firebase', name: 'firebase-admin', version: '6.8.1'
    implementation group: 'com.squareup.okhttp3', name: 'okhttp', version: '4.2.2'
    implementation 'io.github.jav:expo-server-sdk:1.1.0'
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

## ERD

![image](https://user-images.githubusercontent.com/63996052/195288741-af936de7-a23a-49a0-986c-6fa51ba12008.png)

## Architecture

![KakaoTalk_Photo_2022-07-11-20-35-48](https://user-images.githubusercontent.com/59060780/178255707-814eb2ac-be3a-4350-940c-f060890c2420.jpeg)


## Test Coverage

![image](https://user-images.githubusercontent.com/63996052/180591843-3344c378-bd7f-4487-bc08-ce44e0da3fb9.png)


## Awards

| 수상 일자 | 대회명                               | 최종실적                                    | 상금 (만원) |
| :-------- | :----------------------------------- | :------------------------------------------ | :---------- |
| 22.07.14  | SC제일은행 ‘Women in Fintech’        | 최종선정, Creator상 수상 (2위)              | 500         |
| 22.08.05  | 신촌 연합 IT 창업 학회 CEOS 데모데이 | 우수상 수상                                 | 10          |
| 22.08.16  | 신한은행 ‘퓨쳐스랩 8기 뱅크플러스’   | 1차 서류 합격, 2차 면접 탈락                | -           |
| 22.08.19  | 오렌지 플래닛 ‘오렌지 가든’ 6기      | 1차 서류 합격, 2차 인터뷰 합격, 3차 PT 탈락 | -           |
| 22.08.28  | 전국 대학생 창업컨퍼런스 ‘시도’      | 결승진출                                    | -           |
| 22.08.31  | 예비창업패키지 프리스쿨              | 최종선정                                    | 460         |


---

###### 2022 Bankidz Server
