pipeline {
    agent {
        kubernetes {
            yaml """
            apiVersion: v1
            kind: Pod
            spec:
              containers:
              - name: podman
                image: quay.io/podman/stable:latest
                securityContext:
                  privileged: true
                command:
                - sleep
                args:
                - infinity
                volumeMounts:
                - name: podman-graph-storage
                  mountPath: /var/lib/containers

              - name: snyk
                image: snyk/snyk-cli:python-3.6
                command:
                - sleep
                args:
                - infinity
                volumeMounts:
                - name: podman-graph-storage
                  mountPath: /var/lib/containers
              
              volumes:
              - name: podman-graph-storage
                emptyDir: {}
            """
        }
    }

    environment {
        SNYK_TOKEN = credentials('snyk-api-token')  // Reference the Jenkins secret
    }

    stages {
        stage('Check Podman') {
            steps {
                container('podman') {
                    sh 'podman --version'
                }
            }
        } // Check Podman

        stage('Build') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew clean build'
                sh 'ls -l build/libs/'
            }
        } // Build

        stage('Podman Build') {
            steps {
                container('podman') {
                    sh 'podman build -t daundkarash/java-application2_local .'
                    sh 'podman save -o /var/lib/containers/java-application2_local.tar daundkarash/java-application2_local'
                }
            }
        } // Podman Build
        stage('Load Image') {
            steps {
                container('podman') {
                    sh 'podman load -i /var/lib/containers/java-application2_local.tar'
                 }
            }
        }
        stage('Verify Image') {
            steps {
                container('podman') {
                    sh 'podman images | grep daundkarash/java-application2_local'
                    sh 'podman inspect daundkarash/java-application2_local:latest'
                }
            }
        }

        stage('Snyk Container Scan') {
            steps {
                container('snyk') {
                    sh 'snyk auth $SNYK_TOKEN'  // Authenticate with Snyk
                    // sh 'podman load -i /var/lib/containers/java-application2_local.tar'  // Load the image from the .tar file
                    sh 'snyk container test daundkarash/java-application2_local:latest --debug'  // Scan using image tag
                }
            }
        } // Snyk Container Scan

        stage('Push Image to GitLab') {
            steps {
                container('podman') {
                    script {
                        withCredentials([usernamePassword(credentialsId: 'gitlab-registry', usernameVariable: 'GITLAB_USER', passwordVariable: 'GITLAB_TOKEN')]) {
                            sh 'podman login registry.gitlab.com -u ${GITLAB_USER} -p ${GITLAB_TOKEN}'
                            sh 'podman tag daundkarash/java-application2_local registry.gitlab.com/test8011231/jenkins-image-push/java-application2_local:latest'
                            sh 'podman push registry.gitlab.com/test8011231/jenkins-image-push/java-application2_local:latest'
                        }
                    }
                }
            }
        } // Push Image to GitLab
    }
}
