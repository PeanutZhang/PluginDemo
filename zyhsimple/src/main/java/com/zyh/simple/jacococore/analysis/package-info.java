/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/

/**
 * <p>
 * Coverage calculation and analysis. The coverage information is calculated
 * with an {@link com.zyh.simple.jacococore.analysis.Analyzer} instance from class files
 * (target) and {@linkplain com.zyh.simple.jacococore.data.IExecutionDataVisitor execution
 * data} (actual).
 * </p>
 *
 * <p>
 * The {@link com.zyh.simple.jacococore.analysis.CoverageBuilder} creates a hierarchy of
 * {@link com.zyh.simple.jacococore.analysis.ICoverageNode} instances with the following
 * {@link com.zyh.simple.jacococore.analysis.ICoverageNode.ElementType types}:
 * </p>
 *
 * <pre>
 * +-- {@linkplain com.zyh.simple.jacococore.analysis.ICoverageNode.ElementType#GROUP Group} (optional)
 *     +-- {@linkplain com.zyh.simple.jacococore.analysis.ICoverageNode.ElementType#BUNDLE Bundle}
 *         +-- {@linkplain com.zyh.simple.jacococore.analysis.ICoverageNode.ElementType#PACKAGE Package}
 *             +-- {@linkplain com.zyh.simple.jacococore.analysis.ICoverageNode.ElementType#SOURCEFILE Source File}
 *                 +-- {@linkplain com.zyh.simple.jacococore.analysis.ICoverageNode.ElementType#CLASS Class}
 *                     +-- {@linkplain com.zyh.simple.jacococore.analysis.ICoverageNode.ElementType#METHOD Method}
 * </pre>
 */
package com.zyh.simple.jacococore.analysis;
