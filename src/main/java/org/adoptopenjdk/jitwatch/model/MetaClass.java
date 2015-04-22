/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_BYTECODE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_SIG_MATCH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SLASH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.adoptopenjdk.jitwatch.loader.BytecodeLoader;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaClass implements Comparable<MetaClass>
{
	// private static final Logger logger =
	// LoggerFactory.getLogger(MetaClass.class);

	private String className;
	private MetaPackage classPackage;

	private boolean isInterface = false;
	private boolean missingDef = false;

	private List<MetaMethod> classMethods = new CopyOnWriteArrayList<MetaMethod>();
	private List<MetaConstructor> classConstructors = new CopyOnWriteArrayList<MetaConstructor>();

	private int compiledMethodCount = 0;

	private ClassBC classBytecode = null;

	private static final Logger logger = LoggerFactory.getLogger(MetaClass.class);

	public MetaClass(MetaPackage classPackage, String className)
	{
		this.classPackage = classPackage;
		this.className = className;
	}

	public IMetaMember getFirstConstructor()
	{
		IMetaMember result = null;

		for (IMetaMember member : getMetaMembers())
		{
			if (member instanceof MetaConstructor)
			{
				result = member;
				break;
			}
		}

		return result;
	}

	public boolean isInterface()
	{
		return isInterface;
	}

	public void incCompiledMethodCount()
	{
		compiledMethodCount++;
	}

	public boolean hasCompiledMethods()
	{
		return compiledMethodCount > 0;
	}

	public void setInterface(boolean isInterface)
	{
		this.isInterface = isInterface;
	}

	public boolean isMissingDef()
	{
		return missingDef;
	}

	public void setMissingDef(boolean missingDef)
	{
		this.missingDef = missingDef;
	}

	public ClassBC getClassBytecode(List<String> classLocations)
	{
		if (DEBUG_LOGGING_BYTECODE)
		{
			logger.debug("getClassBytecode for {} existing? {}", getName(), classBytecode != null);
		}

		if (classBytecode == null)
		{
			classBytecode = BytecodeLoader.fetchBytecodeForClass(classLocations, getFullyQualifiedName());
		}

		return classBytecode;
	}

	public String toStringDetailed()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(classPackage.getName()).append(S_DOT).append(className).append(C_SPACE).append(compiledMethodCount)
				.append(S_SLASH).append(classMethods.size());

		return builder.toString();
	}

	@Override
	public String toString()
	{
		return getName();
	}

	public String getName()
	{
		return className;
	}

	public String getFullyQualifiedName()
	{
		StringBuilder builder = new StringBuilder();

		if (classPackage != null && classPackage.getName().length() > 0)
		{
			builder.append(classPackage.getName()).append(C_DOT);
		}

		builder.append(className);

		return builder.toString();
	}

	public String getAbbreviatedFullyQualifiedName()
	{
		StringBuilder builder = new StringBuilder();

		if (classPackage != null && classPackage.getName().length() > 0)
		{
			String[] parts = classPackage.getName().split("\\.");

			for (String part : parts)
			{
				builder.append(part.charAt(0)).append(C_DOT);
			}
		}

		builder.append(className);

		return builder.toString();
	}

	public MetaPackage getPackage()
	{
		return classPackage;
	}

	public void addMetaMethod(MetaMethod method)
	{
		classMethods.add(method);
	}

	public void addMetaConstructor(MetaConstructor constructor)
	{
		classConstructors.add(constructor);
	}

	public List<IMetaMember> getMetaMembers()
	{
		List<IMetaMember> result = new ArrayList<>();

		IMetaMember[] constructorsArray = classConstructors.toArray(new MetaConstructor[classConstructors.size()]);
		Arrays.sort(constructorsArray);

		IMetaMember[] methodsArray = classMethods.toArray(new MetaMethod[classMethods.size()]);
		Arrays.sort(methodsArray);

		result.addAll(Arrays.asList(constructorsArray));
		result.addAll(Arrays.asList(methodsArray));

		return result;
	}

	public IMetaMember getMemberForSignature(MemberSignatureParts msp)
	{
		IMetaMember result = null;

		if (DEBUG_LOGGING_SIG_MATCH)
		{
			logger.debug("Comparing: {} members of {}", getMetaMembers().size(), this);
		}

		for (IMetaMember member : getMetaMembers())
		{
			if (member.matchesSignature(msp, true))
			{
				result = member;
				break;
			}
		}

		return result;
	}


	public List<String> getTreePath()
	{
		MetaPackage metaPackage = getPackage();

		List<String> path = metaPackage.getPackageComponents();
		path.add(getName());

		return path;
	}

	@Override
	public int compareTo(MetaClass other)
	{
		return this.getName().compareTo(other.getName());
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		else
		{
			return toString().equals(obj.toString());
		}
	}
}
