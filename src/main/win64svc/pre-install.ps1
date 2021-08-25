Copy-Item -Path ${project.basedir}/src/main/resources/startCaptureScript.ps1 -Destination .
Copy-Item -Path ${project.build.directory}/${project.artifactId}-${project.version}-runner.jar -Destination .
