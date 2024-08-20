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

              - name: trivy
                image: aquasec/trivy:latest
                command:
                - sleep
                args:
                - infinity

              volumes:
              - name: podman-graph-storage
                emptyDir: {}
            """
        }
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
                }
            }
        } // Podman Build

        stage('Container Scanning') {
            steps {
                container('trivy') {
                    sh 'trivy image --severity HIGH,CRITICAL daundkarash/java-application2_local'
                }
            }
        } // Container Scanning

        stage('Push Image to GitLab') {
            steps {
                container('podman') {
                    script {
                        withCredentials([usernamePassword(credentialsId: 'gitlab-registry', usernameVariable: 'GITLAB_USER', passwordVariable: 'GITLAB_TOKEN')]) {
                            // Log in to GitLab Container Registry
                            sh 'podman login registry.gitlab.com -u ${GITLAB_USER} -p ${GITLAB_TOKEN}'
                            
                            // Tag the image for GitLab Container Registry
                            sh 'podman tag daundkarash/java-application2_local registry.gitlab.com/test8011231/jenkins-image-push/java-application2_local:latest'
                            
                            // Push the image to GitLab Container Registry
                            sh 'podman push registry.gitlab.com/test8011231/jenkins-image-push/java-application2_local:latest'
                        }
                    }
                }
            }
        } // Push Image to GitLab
    }
}
