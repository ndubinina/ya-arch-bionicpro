plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
    // Web (Controller)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // JDBC
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    // ClickHouse driver
    implementation("com.clickhouse:clickhouse-jdbc:0.6.0")

    implementation("org.apache.httpcomponents.client5:httpclient5:5.3.1")


//    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // OAuth2 client (самое главное)
//    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
//
//    // security filter chain
//    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.core:jackson-databind")

	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
