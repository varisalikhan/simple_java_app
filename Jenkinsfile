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
        }

        stage('Build') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew clean build'
                sh 'ls -l build/libs/'
            }
        }

        stage('Podman Build') {
            steps {
                container('podman') {
                    sh 'podman build -t daundkarash/java-application2_local .'
                    sh 'podman save -o /var/lib/containers/java-application3_local.tar daundkarash/java-application2_local'
                }
            }
        }

        stage('Load Image') {
            steps {
                container('podman') {
                    sh 'podman load -i /var/lib/containers/java-application3_local.tar'
                }
            }
        }

        stage('Verify Image') {
            steps {
                container('podman') {
                    sh 'podman images | grep daundkarash/java-application2_local'
                }
            }
        }

        stage('Snyk Container Scan') {
            steps {
                container('podman') {
                    snykSecurity additionalArguments: 'docker-archive:/var/lib/containers/java-application3_local.tar', failOnError: false, failOnIssues: false, snykInstallation: 'snyk_cli', snykTokenId: 'Snyk_Token'
                    // snykSecurity(
                    //     snykInstallation: 'snyk_cli',  // Replace with your configured Snyk installation name
                    //     snykTokenId: 'Snyk_Token',  // Referencing the Snyk API token credential ID
                    //     failOnIssues: true,
                    //     failOnError: true,
                    //     monitorProjectOnBuild: false,
                    //     additionalArguments: 'docker-archive:/var/lib/containers/java-application2_local.tar --debug'
                    // )
                }
            }
        }

        // stage('Archive Snyk Results') {
        //     steps {
        //         archiveArtifacts artifacts: 'snyk_scan_results.json', allowEmptyArchive: true
        //     }
        // }
    }

    // post {
    //     always {
    //         archiveArtifacts artifacts: 'snyk_scan_results.json', allowEmptyArchive: true
    //     }
    // }
}
