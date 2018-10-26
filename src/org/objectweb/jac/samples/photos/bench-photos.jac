applicationName: photos
launchingClass: org.objectweb.jac.samples.photos.Bench
aspects: \
  rtti org/objectweb/jac/samples/photos/rtti.acc true \
  integrity org/objectweb/jac/samples/photos/integrity.acc false \
  persistence org/objectweb/jac/samples/photos/persistence-bench.acc true \
  session org/objectweb/jac/samples/photos/session.acc true \
  gui org/objectweb/jac/samples/photos/gui.acc true \
  confirmation org/objectweb/jac/samples/photos/confirmation.acc true \
  authentication org/objectweb/jac/samples/photos/authentication.acc false \
  cache org/objectweb/jac/samples/photos/cache.acc true \
  tracing org/objectweb/jac/samples/photos/tracing.acc true

jac.toNotAdapt: ejp.*