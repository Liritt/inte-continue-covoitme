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
        PROD_USER_HOST = 'urca@10.11.19.51'
        MAVEN_OPTS = "-Xmx1024m"
        CI = "true"
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

                        sh "scp -o StrictHostKeyChecking=no compose.yml ${env.PREPROD_USER_HOST}:~/compose.yml"
                        sh "ssh -o StrictHostKeyChecking=no ${env.PREPROD_USER_HOST} 'mkdir -p ~/src/main/webapp/WEB-INF/sql'"
                        sh "scp -o StrictHostKeyChecking=no src/main/webapp/WEB-INF/sql/init.sql ${env.PREPROD_USER_HOST}:~/src/main/webapp/WEB-INF/sql/init.sql"
                        sh "scp -r -o StrictHostKeyChecking=no load-tests ${env.PREPROD_USER_HOST}:~/load-tests"

                        def deployCmd = """
                            ssh -o StrictHostKeyChecking=no ${env.PREPROD_USER_HOST} '
                                docker compose -f ~/compose.yml pull
                                docker compose -f ~/compose.yml down
                                docker compose -f ~/compose.yml up -d
                            '
                        """
                        sh deployCmd
                    }
                }
            }
        }

        stage('Run Tests') {
            steps {
                sshagent(credentials: [env.PREPROD_SSH_ID]) {
                    script {
                        def testCmd = """
                            ssh -o StrictHostKeyChecking=no ${env.PREPROD_USER_HOST} '
                                docker exec covoitme-app mvn test
                            '
                        """
                        sh testCmd
                    }
                }
            }
        }

        stage('Load Tests') {
            steps {
                sshagent(credentials: [env.PREPROD_SSH_ID]) {
                    script {
                        echo "Running load tests on preprod environment with Docker..."

                        def loadTestCmd = """
                            ssh -o StrictHostKeyChecking=no ${env.PREPROD_USER_HOST} '
                                cd ~/load-tests && \\
                                docker run --rm \\
                                    --network host \\
                                    -v \$(pwd):/mnt/locust \\
                                    -w /mnt/locust \\
                                    locustio/locust \\
                                    -f locustfile.py \\
                                    --host=http://localhost:8080 \\
                                    --headless \\
                                    --users=20 \\
                                    --spawn-rate=5 \\
                                    --run-time=1m \\
                                    --html=report-${BUILD_NUMBER}.html \\
                                    --csv=results-${BUILD_NUMBER} \\
                                    --only-summary
                            '
                        """
                        sh loadTestCmd

                        // Récupérer le rapport HTML
                        sh "scp -o StrictHostKeyChecking=no ${env.PREPROD_USER_HOST}:~/load-tests/report-${BUILD_NUMBER}.html ."
                        archiveArtifacts artifacts: "report-${BUILD_NUMBER}.html", fingerprint: true

                        echo "Load tests completed. Report archived as artifact."
                    }
                }
            }
        }

        // Si le tests réussissent sur la pre-prod, alors on peut déployer l'app sur la prod
        stage('Deploy to Prod') {
            steps {
                sshagent(credentials: [env.PREPROD_SSH_ID]) {
                    script {
                        def imageToDeploy = "${env.IMAGE_NAME}:${env.BUILD_NUMBER}"

                        sh "scp -o StrictHostKeyChecking=no compose.yml ${env.PROD_USER_HOST}:~/compose.yml"
                        sh "ssh -o StrictHostKeyChecking=no ${env.PROD_USER_HOST} 'mkdir -p ~/src/main/webapp/WEB-INF/sql'"
                        sh "scp -o StrictHostKeyChecking=no src/main/webapp/WEB-INF/sql/init.sql ${env.PROD_USER_HOST}:~/src/main/webapp/WEB-INF/sql/init.sql"

                        def deployCmd = """
                            ssh -o StrictHostKeyChecking=no ${env.PROD_USER_HOST} '
                                docker compose -f ~/compose.yml pull
                                docker compose -f ~/compose.yml down
                                docker compose -f ~/compose.yml up -d
                            '
                        """
                        sh deployCmd
                    }
                }
            }
        }
    }
}