package com.wt.studio.plugin.wizard.projects.services.wsdl.wsclient.util;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.exolab.castor.xml.schema.ComplexType;
import org.exolab.castor.xml.schema.ElementDecl;
import org.exolab.castor.xml.schema.Group;
import org.exolab.castor.xml.schema.Particle;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.SimpleTypesFactory;
import org.exolab.castor.xml.schema.Structure;
import org.exolab.castor.xml.schema.XMLType;
import org.jdom.input.DOMBuilder;

import com.wt.studio.plugin.wizard.projects.services.wsdl.wsclient.domain.ComplexObjectInfo;
import com.wt.studio.plugin.wizard.projects.services.wsdl.wsclient.domain.OperationInfo;
import com.wt.studio.plugin.wizard.projects.services.wsdl.wsclient.domain.ParameterInfo;
import com.wt.studio.plugin.wizard.projects.services.wsdl.wsclient.domain.ServiceInfo;

public class ComponentBuilder {

	WSDLFactory wsdlFactory = null;

	SimpleTypesFactory simpleTypesFactory = null;

	private Vector wsdlTypes = new Vector();
	
	private Set<ComplexObjectInfo> list = new HashSet<ComplexObjectInfo>();
	
	public Set<ComplexObjectInfo> getList() {
		return list;
	}

	public final static String DEFAULT_SOAP_ENCODING_STYLE = "http://schemas.xmlsoap.org/wsdl/soap/encoding/";

	public ComponentBuilder() {
		try {
			wsdlFactory = WSDLFactory.newInstance();
			simpleTypesFactory = new SimpleTypesFactory();
		} catch (Throwable t) {
			System.err.println(t.getMessage());
		}
	}

	public ServiceInfo buildserviceinformation(ServiceInfo serviceinfo)
			throws Exception{
		WSDLReader reader=wsdlFactory.newWSDLReader();
		Definition def=reader.readWSDL(serviceinfo.getWsdllocation());
		
		wsdlTypes = createSchemaFromTypes(def);
		System.out.println("WSDL的Schema已经成功构建完,总共有多少个Schema定义:"+wsdlTypes.size());
		
		Map services=def.getServices();
		if (services!= null) {
			Iterator svcIter = services.values().iterator();
			populateComponent(serviceinfo, (Service) svcIter.next());
			System.out.println("***恭喜您!系统的Web服务对象:ServiceInfo已经成功构建***");
			System.out.println("");
		}
		return serviceinfo;
	}

	private Schema createschemafromtype(org.w3c.dom.Element schemaElement,
			Definition wsdlDefinition) {
		System.out.println("现在的Schema还是一个Dom型的<xsd:schema>元素,属性还不够完全,必须构建命名空间等属性");
		System.out.println("使用JDom,先把Dom型的<xsd:schema>元素转换成JDom型...");
		System.out.println("开始构建...");
		if (schemaElement == null) {
			System.err.println("Unable to find schema extensibility element in WSDL");
			return null;
		}
		DOMBuilder domBuilder = new DOMBuilder();
		org.jdom.Element jdomSchemaElement = domBuilder.build(schemaElement);
		if (jdomSchemaElement == null) {
			System.err.println("Unable to read schema defined in WSDL");
			return null;
		}
		Map namespaces = wsdlDefinition.getNamespaces();
		if (namespaces != null && !namespaces.isEmpty()) {
			System.out.println("WSDL文档Definition的所有命名空间为:");
			Iterator nsIter = namespaces.keySet().iterator();
			while (nsIter.hasNext()) {
				String nsPrefix = (String) nsIter.next();
				String nsURI = (String) namespaces.get(nsPrefix);
				System.out.println("命名空间:"+nsPrefix+" "+nsURI);
				if (nsPrefix!=null&&nsPrefix.length() > 0) {
					org.jdom.Namespace nsDecl = org.jdom.Namespace
							.getNamespace(nsPrefix, nsURI);
					jdomSchemaElement.addNamespaceDeclaration(nsDecl);
				}
			}
		}
		jdomSchemaElement.detach();
		Schema schema = null;
		try {
			schema = XMLSupport.convertElementToSchema(jdomSchemaElement);
			schema.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
		}
		catch (Exception e) {
			System.out.println("a");
			System.err.println(e.getMessage());
			System.out.println("a");
		}
		return schema;
	}

	protected Vector createSchemaFromTypes(Definition wsdlDefinition) {
		System.out.println("*****************");
		System.out.println("进入createSchemaFromTypes()方法");
		System.out.println("开始从Types中产生Schema,Definition下types元素的Schema元素个数可以多个,传进来的参数是一个Definition对象:");
		Vector schemas=new Vector();
		org.w3c.dom.Element schemaElementt=null;
		if (wsdlDefinition.getTypes()!= null) {
			Vector schemaExtElem=findExtensibilityElement(wsdlDefinition
					.getTypes().getExtensibilityElements(), "schema");
			for (int i = 0; i < schemaExtElem.size(); i++) {
				ExtensibilityElement schemaElement = (ExtensibilityElement) schemaExtElem
						.elementAt(i);
				if (schemaElement != null
						&& schemaElement instanceof UnknownExtensibilityElement) {
					schemaElementt = ((UnknownExtensibilityElement) schemaElement)
							.getElement();
					Schema schema = createschemafromtype(schemaElementt,
							wsdlDefinition);
					schemas.add(schema);
				}
			}

		}
		System.out.println("*****************");
		return schemas;
	}

	private ServiceInfo populateComponent(ServiceInfo component, Service service) {
		System.out.println("***************************");
		System.out.println("");
		System.out.println("***开始构建系统的Web服务对象:ServiceInfo***");
		QName qName = service.getQName();
		String namespace = qName.getNamespaceURI();
		System.out.println("namespace为:"+namespace);
		String name = qName.getLocalPart();
		System.out.println("name为:"+name);
		component.setName(name);
		Map ports=service.getPorts();
		Iterator portIter = ports.values().iterator();
		while (portIter.hasNext()) {
			Port port = (Port) portIter.next();
			Binding binding = port.getBinding();
			List operations=buildOperations(binding);
			Iterator operIter = operations.iterator();
			while (operIter.hasNext()) {
				OperationInfo operation = (OperationInfo) operIter.next();
				Vector addrElems = findExtensibilityElement(port
						.getExtensibilityElements(), "address");
				ExtensibilityElement element = (ExtensibilityElement) addrElems
						.elementAt(0);
				if (element != null && element instanceof SOAPAddress) {
					SOAPAddress soapAddr = (SOAPAddress) element;
					operation.setTargetURL(soapAddr.getLocationURI());
				}
				component.addOperation(operation);
			}
		}
		return component;
	}

	private List buildOperations(Binding binding) {
       System.out.println("进入buildOperations(Binding binding)方法,构建服务所有的操作对象List buildOperations");
		List operationInfos = new ArrayList();

		List operations=binding.getBindingOperations();

		if (operations!= null && !operations.isEmpty()) {
             
			Vector soapBindingElems = findExtensibilityElement(binding
					.getExtensibilityElements(), "binding");
			String style = "document"; // default
			
			ExtensibilityElement soapBindingElem = (ExtensibilityElement) soapBindingElems
					.elementAt(0);
			if (soapBindingElem != null
					&& soapBindingElem instanceof SOAPBinding) {
				//SOAPBinding类代表的就是<wsdl:binding>下的子元素:<wsdlsoap:binding元素>
				SOAPBinding soapBinding = (SOAPBinding) soapBindingElem;
				style = soapBinding.getStyle();
			}

			Iterator opIter = operations.iterator();

			while (opIter.hasNext()) {
				//BindingOperation类代表的就是<wsdl:binding>下的子元素:<wsdlsoap:operation元素>
				BindingOperation oper = (BindingOperation) opIter.next();
				Vector operElems = findExtensibilityElement(oper
						.getExtensibilityElements(), "operation");
				ExtensibilityElement operElem = (ExtensibilityElement) operElems
						.elementAt(0);
				//SOAPOperation类代表的就是<wsdlsoap:operation>下的子元素:<wsdlsoap:operation/>
				if (operElem != null && operElem instanceof SOAPOperation) {

					OperationInfo operationInfo = new OperationInfo(style);

					buildOperation(operationInfo, oper);

					operationInfos.add(operationInfo);
				}
			}
		}

		return operationInfos;
	}

	private OperationInfo buildOperation(OperationInfo operationInfo,
			BindingOperation bindingOper) {
        System.out.println("从一个BindingOperation对象(<wsdl:operation>)构建OperationInfo对象");
		Operation oper = bindingOper.getOperation();
		operationInfo.setTargetMethodName(oper.getName());
		Vector operElems = findExtensibilityElement(bindingOper
				.getExtensibilityElements(), "operation");
		ExtensibilityElement operElem = (ExtensibilityElement) operElems
				.elementAt(0);
		if (operElem != null && operElem instanceof SOAPOperation) {
			SOAPOperation soapOperation = (SOAPOperation) operElem;
			operationInfo.setSoapActionURI(soapOperation.getSoapActionURI());
		}
		BindingInput bindingInput = bindingOper.getBindingInput();
		BindingOutput bindingOutput = bindingOper.getBindingOutput();
		Vector bodyElems = findExtensibilityElement(bindingInput
				.getExtensibilityElements(), "body");
		ExtensibilityElement bodyElem = (ExtensibilityElement) bodyElems
				.elementAt(0);

		if (bodyElem != null && bodyElem instanceof SOAPBody) {
			SOAPBody soapBody = (SOAPBody) bodyElem;

			List styles = soapBody.getEncodingStyles();
			String encodingStyle = null;

			if (styles != null) {

				encodingStyle = styles.get(0).toString();
			}

			if (encodingStyle == null) {

				encodingStyle = DEFAULT_SOAP_ENCODING_STYLE;
			}

			operationInfo.setEncodingStyle(encodingStyle.toString());

			operationInfo.setTargetObjectURI(soapBody.getNamespaceURI());
		}

		Input inDef = oper.getInput();
        System.out.println("开始转移到了<wsdl:portTyp>结点下的<wsdl:input>");
		if (inDef != null) {
			Message inMsg = inDef.getMessage();
			if (inMsg != null) {
				operationInfo.setInputMessageName(inMsg.getQName().getLocalPart());
                //输入消息的参数构建
				getParameterFromMessage(operationInfo, inMsg, 1);
				System.out.println("***操作:"+operationInfo.getTargetMethodName()+"的所有输入参数已经构建完毕***");
				  System.out.println("");
				operationInfo.setInmessage(inMsg);
			}
		}

		Output outDef = oper.getOutput();

		if (outDef != null) {

			Message outMsg = outDef.getMessage();

			if (outMsg != null) {
				operationInfo.setOutputMessageName(outMsg.getQName()
						.getLocalPart());
                //输出消息的参数构建
				getParameterFromMessage(operationInfo, outMsg, 2);
				System.out.println("***操作:"+operationInfo.getTargetMethodName()+"的所有输出参数已经构建完毕***");
				System.out.println("");
				operationInfo.setOutmessage(outMsg);
			}
		}
		

		return operationInfo;
	}

	private void getParameterFromMessage(OperationInfo operationInfo,
			Message msg, int manner) {
        String tip="";
		System.out.println("*******************");
        if(manner==1){
        	tip="输入";
        }
        else{
        	
        	tip="输出";
        }
        System.out.println("");
		System.out.println("***开始构建"+operationInfo.getTargetMethodName()+"操作的所有消息"+tip+"参数***");
       
		List msgParts = msg.getOrderedParts(null);
		Schema wsdlType = null;
		Iterator iter = msgParts.iterator();
		while (iter.hasNext()) {
			Part part = (Part) iter.next();
			String targetnamespace = "";
			XMLType xmlType=getXMLType(part, wsdlType, operationInfo);
			if (xmlType!=null&&xmlType.isComplexType()) {
				buildComplexParameter((ComplexType) xmlType, operationInfo,
						manner);
			}
			else {
				System.out.print("part所引用的xml元素是简单类型");
				String partName = part.getName();
				ParameterInfo parameter = new ParameterInfo();
				parameter.setName(partName);
				parameter.setKind(part.getTypeName().getLocalPart());
				if (manner == 1) {
					//1表示构建的是操作的输入参数
					operationInfo.addInparameter(parameter);
				} else {
					operationInfo.addOutparameter(parameter);
				}
			}
			operationInfo.setWsdltype(wsdlTypes);

		}

	}

	private void buildComplexParameter(ComplexType type,
			OperationInfo operationInfo, int manner) {
		//XML Schema 规范定义了大量的组件，
		//如schema、complexType、simpleType、group、annotation、include、import、element 和 attribute 等等。
		//particleEnum就是ComplexType下的子元素内容,可以是上面的部分组件组合
		Enumeration particleEnum=type.enumerate();
		//group就是元素(可以是复杂类型)集合
		Group group = null;
		if(!particleEnum.hasMoreElements()){
			System.out.println(operationInfo+"操作不需要输入参数");
		}
		while (particleEnum.hasMoreElements()) {
			System.out.println("这是<complexType>容器下的子元素");
			Particle particle = (Particle) particleEnum.nextElement();
			if (particle instanceof Group) {
				System.out.println("子元素也是一个元素集合(<xsd:element...>)");
				group = (Group) particle;
				break;
			}
		}
		if (group != null) {
			
			Enumeration groupEnum = group.enumerate();
			while (groupEnum.hasMoreElements()) {
					//看看此复杂数据类型的每一个Element情况
				Structure item = (Structure) groupEnum.nextElement();
				if (item.getStructureType()==Structure.ELEMENT) {	
					ElementDecl elementDecl = (ElementDecl) item;
					System.out.println("复杂数据类型的子元素是一个Element:"+elementDecl.getReferenceId());
					System.out.println(elementDecl.getName());
					XMLType xmlType = elementDecl.getType();
					ParameterInfo parameter = new ParameterInfo();
						
						System.out.println("现在开始处理简单数据类型");
						parameter.setName(elementDecl.getName());
						System.out.println("参数名为:" + elementDecl.getName());
						parameter.setKind(elementDecl.getType().getName());
						System.out.println("参数类型为:" + elementDecl.getType().getName());
						if (manner == 1) {
							operationInfo.addInparameter(parameter);
						} else {
							operationInfo.addOutparameter(parameter);
						}
						if (xmlType != null && xmlType.isComplexType()) {
							System.out.println("***"+elementDecl.getReferenceId()+"元素是一个复杂类型,进入递归调用****");
							parameter.setType("Object");
							ComplexObjectInfo com = new ComplexObjectInfo();
							com.setName(elementDecl.getType().getName());
							buildComplexObject((ComplexType)xmlType,operationInfo, com);
							list.add(com);
							
						}	

				}
			}
		
		}
		
	}

	private void buildComplexObject(ComplexType type,
			OperationInfo operationInfo, ComplexObjectInfo obj) {
		//XML Schema 规范定义了大量的组件，
		//如schema、complexType、simpleType、group、annotation、include、import、element 和 attribute 等等。
		//particleEnum就是ComplexType下的子元素内容,可以是上面的部分组件组合
		Enumeration particleEnum=type.enumerate();
		//group就是元素(可以是复杂类型)集合
		Group group = null;
		while (particleEnum.hasMoreElements()) {
			System.out.println("这是<complexType>容器下的子元素");
			Particle particle = (Particle) particleEnum.nextElement();
			if (particle instanceof Group) {
				System.out.println("子元素也是一个元素集合(<xsd:element...>)");
				group = (Group) particle;
				break;
			}
		}
		if (group != null) {
			Enumeration groupEnum = group.enumerate();
			while (groupEnum.hasMoreElements()) {
					//看看此复杂数据类型的每一个Element情况
				Structure item = (Structure) groupEnum.nextElement();
				if (item.getStructureType()==Structure.ELEMENT) {	
					ElementDecl elementDecl = (ElementDecl) item;
					System.out.println("复杂数据类型的子元素是一个Element:"+elementDecl.getReferenceId());
					System.out.println(elementDecl.getName());
					XMLType xmlType = elementDecl.getType();
					ParameterInfo parameter = new ParameterInfo();
					if (xmlType != null && xmlType.isComplexType()) {
						System.out.println("***"+elementDecl.getReferenceId()+"元素是一个复杂类型,进入递归调用****");
						parameter.setType("Object");
						buildComplexObject((ComplexType)xmlType,operationInfo, obj);
					}	
						
						System.out.println("现在开始处理简单数据类型");
						parameter.setName(elementDecl.getName());
						System.out.println("参数名为:" + elementDecl.getName());
						parameter.setKind(elementDecl.getType().getName());
						System.out.println("参数类型为:" + elementDecl.getType().getName());
						obj.addFiled(parameter);

				}
			}
		
		}
	}

	protected XMLType getXMLType(Part part, Schema wsdlType,
			OperationInfo operationInfo) {
		if (wsdlTypes == null) {
			System.out.println("null is here in the 1 ");
			return null;
		}

		XMLType xmlType = null;

		if (part.getElementName()!= null) {
			String elemName=part.getElementName().getLocalPart();
			System.out.println("part引用的类型名为:"+elemName);
			ElementDecl elemDecl = null;
			for (int i = 0; i < wsdlTypes.size(); i++) {
				wsdlType = (Schema) (wsdlTypes.elementAt(i));
				String targetnamespace=wsdlType.getTargetNamespace();
				operationInfo.setNamespaceURI(targetnamespace);
				elemDecl=wsdlType.getElementDecl(elemName);
				if (elemDecl!=null) {
					break;
				}

			}
			if (elemDecl!=null) {
				 xmlType = elemDecl.getType();
			}
		
		}

		return xmlType;
	}

	private static Vector findExtensibilityElement(List extensibilityElements,
			String elementType) {
		int i = 0;
		Vector elements = new Vector();
		if (extensibilityElements!= null) {
			Iterator iter = extensibilityElements.iterator();
			while (iter.hasNext()) {
				ExtensibilityElement elment = (ExtensibilityElement)iter.next();
				if (elment.getElementType().getLocalPart().equalsIgnoreCase(elementType)) {
					elements.add(elment);
				}
			}
		}
		return elements;
	}
}
