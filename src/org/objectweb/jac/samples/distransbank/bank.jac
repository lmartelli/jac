applicationName: distransbank
launchingClass: org.objectweb.jac.samples.distransbank.Account
aspects: \
	distrans-persistence distrans-persistence.acc true \
	distrans distrans.acc true
	
jac.acs: \
	distrans org.objectweb.jac.aspects.distrans.DisTransAC \
	distrans-persistence org.objectweb.jac.aspects.distrans.persistence.PersistenceAC

jac.comp.wrappingOrder: \
	org.objectweb.jac.aspects.distrans.BeginTransactionWrapper \
	org.objectweb.jac.aspects.distrans.EndTransactionWrapper \
	org.objectweb.jac.aspects.distrans.persistence.ReadWrapper \
	org.objectweb.jac.aspects.distrans.persistence.WriteWrapper

jac.toNotAdapt: \
	org.objectweb.* \
	org.enhydra.* \
	org.apache.* \
	net.hibernate.* \
	java.* \
	javax.* \
	org.postgresql.* \
	sun.* \
	com.sun.* \
	gnu.* \
	antlr.* \
	java_cup.* \
	org.w3c.dom.* \
	org.apache.xerces.* \
	org.xml.sax.* \
	CH.ifa.draw.* \
	aspectBlock1.graphic.* \
	aspectBlock1.data.DataValueVector \
	aspectBlock1.data.TransferSetVector \
	aspectBlock1.server.ScheduleServer \
	org.mortbay.* \
	com.incors.*

jac.toAdapt: \
	org.objectweb.jac.samples.* \
	org.objectweb.jac.util.Repository \
	org.objectweb.jac.util.Thumbnail \
	org.objectweb.jac.core.ApplicationRepository \
	org.objectweb.jac.core.ACParser \
	org.objectweb.jac.core.ParserImpl \
	org.objectweb.jac.aspects.tracing.Debugger \
	org.objectweb.jac.aspects.tracing.Recorder \
	org.objectweb.jac.core.NameRepository \
   	org.objectweb.jac.core.dist.Topology \
   	org.objectweb.jac.aspects.distribution.thinclient.AbstractServer \
	org.objectweb.jac.aspects.gui.Actions \
	org.objectweb.jac.aspects.gui.WrappableMap \
	org.objectweb.jac.aspects.gui.CollectionWrapper \
	org.objectweb.jac.aspects.gui.TableModel \
	org.objectweb.jac.aspects.gui.swing.SwingDisplay \
	org.objectweb.jac.aspects.gui.web.WebDisplay \
	org.objectweb.jac.aspects.user.UserManager \
	org.objectweb.jac.aspects.user.Profile \
	org.objectweb.jac.aspects.user.Rule \
	org.objectweb.jac.aspects.idGen.Counters
	