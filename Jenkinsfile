pipeline {
    agent any
    tools {
        maven 'Maven 3.8.6'
    }

    environment {
        REGISTRY_URL = 'https://ghcr.io'
        REGISTRY_CREDENTIAL_ID = 'github-ghcr-credentials'
        IMAGE_NAME = "ghcr.io/liritt/inte-continue-covoitme"
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/Liritt/inte-continue-covoitme.git', branch: 'main'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn --version'
                sh 'mvn clean install' 
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'target/*.war', fingerprint: true
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def dockerImage = "${env.IMAGE_NAME}:${env.BUILD_NUMBER}"
                    echo "Construction de l'image : ${dockerImage}"

                    docker.withRegistry(env.REGISTRY_URL, env.REGISTRY_CREDENTIAL_ID) {
                        def img = docker.build(dockerImage, '.')
                        img.push()
                        img.push('latest')
                    }
                }
            }
        }
    }
}