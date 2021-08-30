Copy-Item -Path ${project.basedir}/src/main/resources/startCaptureScript.ps1 -Destination .
Copy-Item -Path ${project.build.directory}/${project.artifactId}-${project.version}-runner.jar -Destination .
Copy-Item -Path ${project.basedir}/.env -Destination .
Copy-Item -Path ${project.basedir}/admin_scripts/add-all-filters.ps1 -Destination .
Copy-Item -Path ${project.basedir}/admin_scripts/add-filter.ps1 -Destination .
Copy-Item -Path ${project.basedir}/admin_scripts/delete-filter.ps1 -Destination .
