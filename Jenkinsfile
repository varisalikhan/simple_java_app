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
                // Run the Gradle build command in the Podman container
                // sh './gradlew clean build --stacktrace -i'
                sh './gradlew clean build '
                sh 'ls -l build/libs/'
            }
         } // Build

        stage('Podman Build') {
            steps {
                // Build the container image using Podman
                container('podman') {
                    sh 'podman build -t daundkarash/java-application2_local .'
                }
            }
         } // Podman Build docker image
        
         stage('container scanning') {
            steps {
                sh 'trivy daundkarash/java-application2_local'
            }
         }
        stage('Push image to GitLab') {
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
         } // Push image to GitLab
    }
}
