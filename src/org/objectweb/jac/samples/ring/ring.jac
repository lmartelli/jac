applicationName: ring
launchingClass: org.objectweb.jac.samples.ring.Run

aspects: \
  deployment org/objectweb/jac/samples/ring/deployment.acc false \
  tracing org/objectweb/jac/samples/ring/tracing.acc false

topology: //localhost/s1 //localhost/s2 //localhost/s3
