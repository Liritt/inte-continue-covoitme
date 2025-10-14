pipeline {
    agent any
    tools {
        maven 'Maven 3.8.6'
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
    }
}