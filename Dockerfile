FROM eclipse-temurin:17.0.2_8-jdk-focal

# need to install git for paper plugin
RUN apt update -y; \
    apt install git -y; \
    git config --global user.email "ci@example.com"; \
    git config --global user.name "Continuous Integration"
