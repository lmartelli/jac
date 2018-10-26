applicationName: hibernate-sample
launchingClass: org.objectweb.jac.samples.hibernate.Account
aspects: \
	hibernate hibernate.acc true
	
jac.acs: \
	hibernate org.objectweb.jac.aspects.hibernate.HibernateAC

jac.comp.wrappingOrder: \
	org.objectweb.jac.aspects.hibernate.BeginPersistentSessionWrapper \
	org.objectweb.jac.aspects.hibernate.EndPersistentSessionWrapper

jac.toNotAdapt: \
	org.objectweb.* \
	org.enhydra.* \
	org.apache.* \
	net.sf.* \
	org.dom4j.* \
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
	