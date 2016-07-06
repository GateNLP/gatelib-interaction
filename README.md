# gatelib-interaction

A small and light-weight library for some often-used ways of interacting between GATE (Java) 
and other software through command line and pipes or simple HTTP services.

# Compilation/Installation with Maven:

This includes a small test which already requires the compiled jar and the downloaded
dependencies in the target 
directory. When "mvn install" is run for the first time, the jar is not already 
there, so the test will fail, so the jar will not be created.

The solution is to run 
  mvn install -Dmaven.test.skip=true 
  mvn dependency:copy-dependencies
or 
  mvn package -Dmaven.test.skip=true
  mvn dependency:copy-dependencies
once. Subsequent runs of "mvn install" should then work and also run the 
tests successfully.
