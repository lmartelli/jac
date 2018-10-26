applicationName: photos
launchingClass: org.objectweb.jac.samples.photos.Run
aspects: \
  rtti org/objectweb/jac/samples/photos/rtti.acc true \
  integrity org/objectweb/jac/samples/photos/integrity.acc true \
  persistence org/objectweb/jac/samples/photos/persistence.acc true \
  session org/objectweb/jac/samples/photos/session.acc true \
  confirmation org/objectweb/jac/samples/photos/confirmation.acc true \
  gui org/objectweb/jac/samples/photos/gui.acc true \
  org.objectweb.jac.samples.photos.CompUserAC org/objectweb/jac/samples/photos/comp-user.acc true \
  cache org/objectweb/jac/samples/photos/cache.acc true \
  tracing org/objectweb/jac/samples/photos/tracing.acc true

#  tracing org/objectweb/jac/samples/photos/tracing.acc true \
#  authentication org/objectweb/jac/samples/photos/authentication.acc false \
#  tracing org/objectweb/jac/samples/photos/tracing.acc false
#
#  authentication org/objectweb/jac/samples/photos/authentication.acc true \
#  user org/objectweb/jac/samples/photos/user.acc true \
