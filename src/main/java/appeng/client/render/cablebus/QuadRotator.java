/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.render.cablebus;

import java.util.EnumMap;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.core.Direction;

import appeng.client.render.FacingToRotation;
import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.RenderContext;

/**
 * Assuming a default-orientation of forward=NORTH and up=UP, this class rotates a given list of quads to the desired
 * facing
 */
public class QuadRotator implements RenderContext.QuadTransform {

    public static final RenderContext.QuadTransform NULL_TRANSFORM = quad -> true;

    private static final EnumMap<FacingToRotation, RenderContext.QuadTransform> TRANSFORMS = new EnumMap<>(
            FacingToRotation.class);

    static {
        for (FacingToRotation rotation : FacingToRotation.values()) {
            if (rotation.isRedundant()) {
                TRANSFORMS.put(rotation, NULL_TRANSFORM);
            } else {
                TRANSFORMS.put(rotation, new QuadRotator(rotation));
            }
        }
    }

    private final FacingToRotation rotation;

    private final Quaternion quaternion;

    private QuadRotator(FacingToRotation rotation) {
        this.rotation = rotation;
        this.quaternion = rotation.getRot();
    }

    public static RenderContext.QuadTransform get(Direction newForward, Direction newUp) {
        return get(getRotation(newForward, newUp));
    }

    public static RenderContext.QuadTransform get(FacingToRotation rotation) {
        if (rotation.isRedundant()) {
            return NULL_TRANSFORM; // This is the default orientation
        }
        return TRANSFORMS.get(rotation);
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        Vector3f tmp = new Vector3f();

        for (int i = 0; i < 4; i++) {
            // Transform the position (center of rotation is 0.5, 0.5, 0.5)
            quad.copyPos(i, tmp);
            tmp.add(-0.5f, -0.5f, -0.5f);
            tmp.transform(quaternion);
            tmp.add(0.5f, 0.5f, 0.5f);
            quad.pos(i, tmp);

            // Transform the normal
            if (quad.hasNormal(i)) {
                quad.copyNormal(i, tmp);
                tmp.transform(quaternion);
                quad.normal(i, tmp);
            }
        }

        // Transform the nominal face, setting the cull face will also overwrite the
        // nominialFace,
        // hence saving both first and the order here.
        Direction nominalFace = quad.nominalFace();
        Direction cullFace = quad.cullFace();
        if (cullFace != null) {
            quad.cullFace(rotation.rotate(cullFace));
        }
        var rotatedNominalFace = rotation.rotate(nominalFace);
        quad.nominalFace(rotatedNominalFace);

        // The vanilla lighting engine expects the vertices of each quad
        // in a specific order for each cardinal direction.
        var data = new int[DefaultVertexFormat.BLOCK.getIntegerSize() * 4];
        quad.toVanilla(0, data, 0, false);
        BlockModel.FACE_BAKERY.recalculateWinding(data, rotatedNominalFace);
        quad.fromVanilla(data, 0, false);

        return true;
    }

    private static FacingToRotation getRotation(Direction forward, Direction up) {
        // Sanitize forward/up
        if (forward.getAxis() == up.getAxis()) {
            if (up.getAxis() == Direction.Axis.Y) {
                up = Direction.NORTH;
            } else {
                up = Direction.UP;
            }
        }

        return FacingToRotation.get(forward, up);
    }

}
