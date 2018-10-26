applicationName: bank

launchingClass: org.objectweb.jac.samples.bank.Run

aspects: \
  rtti org/objectweb/jac/samples/bank/rtti.acc true \
  gui org/objectweb/jac/samples/bank/gui.acc true \
  persistence org/objectweb/jac/samples/bank/persistence.acc true \
  session org/objectweb/jac/samples/bank/session.acc true \
  authentication org/objectweb/jac/samples/bank/authentication.acc false \
  tracing org/objectweb/jac/samples/bank/tracing.acc false \
  synchronization org/objectweb/jac/samples/bank/synchronization.acc false \
  transaction org/objectweb/jac/samples/bank/transaction.acc true \
  deployment org/objectweb/jac/samples/bank/deployment.acc false \
  consistency org/objectweb/jac/samples/bank/consistency.acc false

