/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2016, 2018 by the contributors of the JetUML project.
 *
 * See: https://github.com/prmr/JetUML
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
/**
 * 
 */
package ca.mcgill.cs.jetuml.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.mcgill.cs.jetuml.JavaFXLoader;
import ca.mcgill.cs.jetuml.diagram.ClassDiagram;
import ca.mcgill.cs.jetuml.diagram.builder.CompoundOperation;
import ca.mcgill.cs.jetuml.diagram.builder.DiagramOperation;
import ca.mcgill.cs.jetuml.diagram.edges.DependencyEdge;
import ca.mcgill.cs.jetuml.diagram.nodes.ClassNode;
import ca.mcgill.cs.jetuml.gui.SelectionModel;

public class TestMoveTracker
{
	private MoveTracker aMoveTracker;
	private SelectionModel aSelection;
	private ClassDiagram aDiagram;
	private ClassNode aNode1; // Initial bounds: [x=150.0,y=150.0,w=100.0,h=60.0]
	private ClassNode aNode2; // Initial bounds: [x=400.0,y=400.0,w=100.0,h=60.0]
	private DependencyEdge aEdge1;
	private Field aOperationsField;
	
	/**
	 * Load JavaFX toolkit and environment.
	 */
	@BeforeClass
	@SuppressWarnings("unused")
	public static void setupClass()
	{
		JavaFXLoader loader = JavaFXLoader.instance();
	}
	
	@Before
	public void setup() throws ReflectiveOperationException
	{
		aMoveTracker = new MoveTracker();
		aSelection = new SelectionModel( () -> {} );
		aDiagram = new ClassDiagram();
		aNode1 = new ClassNode();
		aNode1.translate(150, 150);
		aNode2 = new ClassNode();
		aNode2.translate(400, 400);
		aEdge1 = new DependencyEdge();
		aDiagram.restoreEdge(aEdge1, aNode1, aNode1);
		aOperationsField = CompoundOperation.class.getDeclaredField("aOperations");
		aOperationsField.setAccessible(true);
	}

	@Test
	public void moveSingleObjectFourTimes()
	{
		aSelection.addToSelection(aNode1);
		aMoveTracker.startTrackingMove(aSelection);
		aNode1.translate(20, 20);
		aNode1.translate(0, 200);
		aNode1.translate(50, 50);
		CompoundOperation operation = aMoveTracker.endTrackingMove(aDiagram);
		assertEquals(1, getOperations(operation).size());
		operation.undo();
		assertEquals(150, aNode1.position().getX());
		assertEquals(150, aNode1.position().getY());
		operation.execute();
		assertEquals(220, aNode1.position().getX());
		assertEquals(420, aNode1.position().getY());
		
		// No change in selection, move only X
		aMoveTracker.startTrackingMove(aSelection);
		aNode1.translate(200, 0);
		operation = aMoveTracker.endTrackingMove(aDiagram);
		assertEquals(1, getOperations(operation).size());
		operation.undo();
		assertEquals(220, aNode1.position().getX());
		assertEquals(420, aNode1.position().getY());
		operation.execute();
		assertEquals(420, aNode1.position().getX());
		assertEquals(420, aNode1.position().getY());
		
		// No change in selection, move only Y
		aMoveTracker.startTrackingMove(aSelection);
		aNode1.translate(0, 200);
		operation = aMoveTracker.endTrackingMove(aDiagram);
		assertEquals(1, getOperations(operation).size());
		operation.undo();
		assertEquals(420, aNode1.position().getX());
		assertEquals(420, aNode1.position().getY());
		operation.execute();
		assertEquals(420, aNode1.position().getX());
		assertEquals(620, aNode1.position().getY());
		
		// No change in selection, null move
		aMoveTracker.startTrackingMove(aSelection);
		aNode1.translate(0, 0);
		operation = aMoveTracker.endTrackingMove(aDiagram);
		assertEquals(0, getOperations(operation).size());
	}
	
	@Test
	public void moveNodesAndEdges()
	{
		aSelection.addToSelection(aNode1);
		aSelection.addToSelection(aNode2);
		aSelection.addToSelection(aEdge1);
		aMoveTracker.startTrackingMove(aSelection);
		aNode1.translate(20, 20);
		aNode2.translate(20, 20);
		CompoundOperation operation = aMoveTracker.endTrackingMove(aDiagram);
		List<DiagramOperation> operations = getOperations(operation);
		assertEquals(2, operations.size());
		
		operations.get(0).undo();
		assertEquals(150, aNode1.position().getX());
		assertEquals(150, aNode1.position().getY());
		assertEquals(420, aNode2.position().getX());
		assertEquals(420, aNode2.position().getY());
		operations.get(0).execute();
		assertEquals(170, aNode1.position().getX());
		assertEquals(170, aNode1.position().getY());
		assertEquals(420, aNode2.position().getX());
		assertEquals(420, aNode2.position().getY());
		
		operations.get(1).undo();
		assertEquals(170, aNode1.position().getX());
		assertEquals(170, aNode1.position().getY());
		assertEquals(400, aNode2.position().getX());
		assertEquals(400, aNode2.position().getY());
		operations.get(1).execute();
		assertEquals(170, aNode1.position().getX());
		assertEquals(170, aNode1.position().getY());
		assertEquals(420, aNode2.position().getX());
		assertEquals(420, aNode2.position().getY());

		// Second identical move
		aMoveTracker.startTrackingMove(aSelection);
		aNode1.translate(20, 20);
		aNode2.translate(20, 20);
		operation = aMoveTracker.endTrackingMove(aDiagram);
		
		operations = getOperations(operation);
		assertEquals(2, operations.size());
		
		operations.get(0).undo();
		assertEquals(170, aNode1.position().getX());
		assertEquals(170, aNode1.position().getY());
		assertEquals(440, aNode2.position().getX());
		assertEquals(440, aNode2.position().getY());
		operations.get(0).execute();
		assertEquals(190, aNode1.position().getX());
		assertEquals(190, aNode1.position().getY());
		assertEquals(440, aNode2.position().getX());
		assertEquals(440, aNode2.position().getY());
		
		operations.get(1).undo();
		assertEquals(190, aNode1.position().getX());
		assertEquals(190, aNode1.position().getY());
		assertEquals(420, aNode2.position().getX());
		assertEquals(420, aNode2.position().getY());
		operations.get(1).execute();
		assertEquals(190, aNode1.position().getX());
		assertEquals(190, aNode1.position().getY());
		assertEquals(440, aNode2.position().getX());
		assertEquals(440, aNode2.position().getY());
	}
	
	@SuppressWarnings("unchecked")
	private List<DiagramOperation> getOperations(CompoundOperation pOperation)
	{
		try
		{
			return (List<DiagramOperation>)aOperationsField.get(pOperation);
		}
		catch( ReflectiveOperationException pException )
		{
			fail();
			return null;
		}
	}
}
