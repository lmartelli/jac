
applicationName: ide
launchingClass: org.objectweb.jac.ide.Projects
aspects: \
  rtti org/objectweb/jac/ide/rtti.acc true \
  gui org/objectweb/jac/ide/gui.acc true \
  session org/objectweb/jac/ide/session.acc true \
  persistence org/objectweb/jac/ide/persistence.acc true \
  tracing  org/objectweb/jac/ide/tracing.acc true \
  confirmation org/objectweb/jac/ide/confirmation.acc true \
  export org/objectweb/jac/ide/export.acc true \
  integrity org/objectweb/jac/ide/integrity.acc true \
  timestamp org/objectweb/jac/ide/timestamp.acc true \
  cache org/objectweb/jac/ide/cache.acc true

jac.toNotAdapt: \
	org.objectweb.jac.ide.diagrams.* \
	org.objectweb.jac.ide.*Plugin \
	org.objectweb.jac.ide.AccGenState \
	org.objectweb.jac.ide.swing.* \
	dori.* \
	org.apache.* \
	com.lowagie.* \
	org.objectweb.jac.ide.MethodItemConverter

jac.toAdapt: \
	org.objectweb.jac.ide.diagrams.ClassFigureCreationTool \
	org.objectweb.jac.ide.diagrams.NewClassFigureCreationTool \
	org.objectweb.jac.ide.diagrams.NewAspectFigureCreationTool \
	org.objectweb.jac.ide.diagrams.AspectFigureCreationTool \
	org.objectweb.jac.ide.diagrams.NewInstanceFigureCreationTool \
	org.objectweb.jac.ide.diagrams.InstanceFigureCreationTool \
	org.objectweb.jac.ide.diagrams.NewGroupFigureCreationTool \
	org.objectweb.jac.ide.diagrams.GroupFigureCreationTool \
	org.objectweb.jac.ide.diagrams.RelationLinkShowTool
