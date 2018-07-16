/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2015-2018 by the contributors of the JetUML project.
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
package ca.mcgill.cs.jetuml.application;

import java.util.ArrayList;
import java.util.List;

import ca.mcgill.cs.jetuml.diagram.Diagram;
import ca.mcgill.cs.jetuml.diagram.DiagramElement;
import ca.mcgill.cs.jetuml.diagram.Node;
import ca.mcgill.cs.jetuml.diagram.builder.CompoundOperation;
import ca.mcgill.cs.jetuml.geom.Conversions;
import javafx.geometry.Rectangle2D;

/**
 * Tracks the movement of a set of selected diagram elements.
 */
public class MoveTracker
{
	private List<Node> aTrackedNodes = new ArrayList<>();
	private List<Rectangle2D> aOriginalBounds = new ArrayList<>();

	/**
	 * Records the elements in pSelectedElements and their position at the 
	 * time where the method is called.
	 * 
	 * @param pSelectedElements The elements that are being moved. Not null.
	 */
	public void startTrackingMove(Iterable<DiagramElement> pSelectedElements)
	{
		assert pSelectedElements != null;
		
		aTrackedNodes.clear();
		aOriginalBounds.clear();
		
		for(DiagramElement element : pSelectedElements)
		{
			assert element != null;
			if(element instanceof Node)
			{
				aTrackedNodes.add((Node) element);
				aOriginalBounds.add(Conversions.toRectangle2D(((Node)element).view().getBounds()));
			}
		}
	}

	/**
	 * Creates and returns a CompoundOperation that represents the movement
	 * of all tracked nodes between the time where startTrackingMove was 
	 * called and the time endTrackingMove was called.
	 * 
	 * @param pDiagram The Diagram containing the selected elements.
	 * @return A CompoundCommand describing the move.
	 */
	public CompoundOperation endTrackingMove(Diagram pDiagram)
	{
		CompoundOperation operation = new CompoundOperation();
		Rectangle2D[] selectionBounds2 = new Rectangle2D[aOriginalBounds.size()];
		int i = 0;
		for(Node node : aTrackedNodes)
		{
			selectionBounds2[i] = Conversions.toRectangle2D(node.view().getBounds());
			i++;
		}
		for(i = 0; i < aOriginalBounds.size(); i++)
		{
			int dY = (int)(selectionBounds2[i].getMinY() - aOriginalBounds.get(i).getMinY());
			int dX = (int)(selectionBounds2[i].getMinX() - aOriginalBounds.get(i).getMinX());
			if(dX != 0 || dY != 0)
			{
				operation.add(pDiagram.builder().createMoveNodeOperation(aTrackedNodes.get(i), dX, dY));
			}
		}
		return operation;
	}
}
