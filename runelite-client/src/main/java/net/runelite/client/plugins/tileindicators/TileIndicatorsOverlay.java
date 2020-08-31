/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.tileindicators;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Tile;
import net.runelite.api.World;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class TileIndicatorsOverlay extends Overlay
{
	private final Client client;
	private final TileIndicatorsConfig config;

	@Inject
	private TileIndicatorsOverlay(Client client, TileIndicatorsConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.highlightHoveredTile())
		{
			// If we have tile "selected" render it
			if (client.getSelectedSceneTile() != null)
			{
				renderTile(graphics, client.getSelectedSceneTile().getLocalLocation(), config.highlightHoveredColor());
			}
		}

		if (config.highlightHoveredPathTiles())
		{
			WorldPoint currPoint = client.getLocalPlayer().getWorldLocation();
			Tile selectedTile = client.getSelectedSceneTile();
			if (selectedTile != null && currPoint != null)
			{
				WorldPoint selectedPoint = selectedTile.getWorldLocation();
				int xDist = selectedPoint.getX() - currPoint.getX();
				int yDist = selectedPoint.getY() - currPoint.getY();
				int dx = 0, dy = 0;
				if (Math.abs(xDist) > Math.abs(yDist))
				{
					dx = (int) Math.signum((float) xDist);
				}
				else
				{
					dy = (int) Math.signum((float) yDist);
				}
				while (Math.abs(xDist) != Math.abs(yDist))
				{
					currPoint = currPoint.dx(dx).dy(dy);
					LocalPoint pt = LocalPoint.fromWorld(client, currPoint.getX(), currPoint.getY());
					renderTile(graphics, pt, config.highlightHoveredPathColor());
					xDist = selectedPoint.getX() - currPoint.getX();
					yDist = selectedPoint.getY() - currPoint.getY();
				}
				dx = (int) Math.signum((float) xDist);
				dy = (int) Math.signum((float) yDist);
				while (xDist != 0 && yDist != 0)
				{
					currPoint = currPoint.dx(dx).dy(dy);
					LocalPoint pt = LocalPoint.fromWorld(client, currPoint.getX(), currPoint.getY());
					renderTile(graphics, pt, config.highlightHoveredPathColor());
					xDist = selectedPoint.getX() - currPoint.getX();
					yDist = selectedPoint.getY() - currPoint.getY();
				}
			}
		}

		if (config.highlightDestinationTile())
		{
			renderTile(graphics, client.getLocalDestinationLocation(), config.highlightDestinationColor());
		}

		if (config.highlightCurrentTile())
		{
			final WorldPoint playerPos = client.getLocalPlayer().getWorldLocation();
			if (playerPos == null)
			{
				return null;
			}

			final LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPos);
			if (playerPosLocal == null)
			{
				return null;
			}

			renderTile(graphics, playerPosLocal, config.highlightCurrentColor());
		}

		return null;
	}

	private void renderTile(final Graphics2D graphics, final LocalPoint dest, final Color color)
	{
		if (dest == null)
		{
			return;
		}

		final Polygon poly = Perspective.getCanvasTilePoly(client, dest);

		if (poly == null)
		{
			return;
		}

		OverlayUtil.renderPolygon(graphics, poly, color);
	}
}
