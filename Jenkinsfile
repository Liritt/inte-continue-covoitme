pipeline {
    agent any
    tools {
        maven 'Maven 3.8.6'
    }

    environment {
        REGISTRY_URL = 'https://ghcr.io'
        REGISTRY_CREDENTIAL_ID = 'github-ghcr-credentials'
        IMAGE_NAME = "ghcr.io/liritt/inte-continue-covoitme"
        PREPROD_SSH_ID = 'preprod-ssh-key'
        PREPROD_USER_HOST = 'urca@10.11.19.50'
        CONTAINER_NAME = 'covoitme-preprod'
        MAVEN_OPTS = "-Xmx1024m"
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
                sh 'mvn clean package -DskipTests' 
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

        stage('Deploy to Preprod') {
            steps {
                sshagent(credentials: [env.PREPROD_SSH_ID]) {
                    script {
                        def imageToDeploy = "${env.IMAGE_NAME}:${env.BUILD_NUMBER}"
                        def deployCmd = """
                            ssh -o StrictHostKeyChecking=no ${env.PREPROD_USER_HOST} '
                                echo "--- Déploiement sur Preprod ---"
                                echo "Téléchargement de la nouvelle image"
                                docker pull ${imageToDeploy}
                                echo "Arrêt de l ancien conteneur"
                                docker stop ${env.CONTAINER_NAME} || true
                                docker rm ${env.CONTAINER_NAME} || true
                                echo "Démarrage du nouveau conteneur"
                                docker run -d --name ${env.CONTAINER_NAME} -p 8080:8080 ${imageToDeploy}
                                docker run -d --name covoitme-db -e POSTGRES_DB=covoitme -e POSTGRES_USER=covoitme -e POSTGRES_PASSWORD=password -v db-data:/var/lib/postgresql/data -v init-scripts:/docker-entrypoint-initdb.d -p 5432:5432 --restart always postgres:17.5
                            '
                        """
                        sh deployCmd
                    }
                }
            }
        }
    }
}