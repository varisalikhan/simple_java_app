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
                sh './gradlew clean build'
                sh 'ls -l build/libs/'
            }
        } // Build

        stage('Podman Build') {
            steps {
                container('podman') {
                    sh 'podman build -t varisalikhan/java-application2_local .'
                }
            }
        } // Podman Build

        stage('Save Image') {
            steps {
                container('podman') {
                    sh 'podman save -o /var/lib/containers/java-application2_local.tar varisalikhan/java-application2_local'
                }
            }
        } // Save Image
stage('Container Scanning') {
    steps {
        container('trivy') {
            sh '''
                apk add --no-cache jq
                trivy image --input /var/lib/containers/java-application2_local.tar --format json --output trivy-report.json
                cat trivy-report.json | jq -r ".Results[] | \\"<h2>\\(.Target)</h2><pre>\\(.Vulnerabilities[] | .Severity + \\" - \\" + .Title + \\"\\n\\" + .Description)</pre>\\"" > trivy-report.html
            '''
        }
    }
}

// stage('Container Scanning') {
//     steps {
//         container('trivy') {
//             sh '''
//                 trivy image --input /var/lib/containers/java-application2_local.tar --format json --output trivy-report.json
//                 # Optionally, convert JSON to HTML in a simpler way
//                 echo "<html><body><pre>" > trivy-report.html
//                 cat trivy-report.json >> trivy-report.html
//                 echo "</pre></body></html>" >> trivy-report.html
//             '''
//         }
//     }
// }

    }

   post {
    always {
        publishHTML([
            allowMissing: false,
            reportName: 'Trivy Scan Results',      // Title in Jenkins UI
            reportDir: '',                         // Directory where the HTML report is saved
            reportFiles: 'trivy-report.html',      // HTML file to publish
            keepAll: true,                         // Keep reports for all builds
            alwaysLinkToLastBuild: true            // Link to the latest build report
        ])
    }
}


}
