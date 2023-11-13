package net.mehvahdjukaar.moyai.forge;

import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class MoyaiFireworkShape {
    public static final RandomSource rand = RandomSource.create();

    public static void create(FireworkParticles.Starter starter, boolean trail,
                                       boolean flicker, int[] colors, int[] fadeColors) {

        var array = new double[][]
                {{-0.2261,0.4488},{-0.0770,0.4611},{-0.0920,-0.4950},{-0.2472,-0.4847},{-0.3860,-0.4433},{-0.4097,-0.3255},{-0.4036,-0.1809},{-0.3924,-0.0361},{-0.3812,0.1088},{-0.3700,0.2535},{-0.3412,0.3956},{-0.2049,0.2527},{-0.0665,0.2668},{-0.0727,0.1623},{-0.0877,0.0469},{-0.1077,-0.0784},{-0.0717,-0.2167},{-0.2158,-0.2338},{0.2261,0.4488},{0.0770,0.4611},{0.0920,-0.4950},{0.2472,-0.4847},{0.3860,-0.4433},{0.4097,-0.3255},{0.4036,-0.1809},{0.3924,-0.0361},{0.3812,0.1088},{0.3700,0.2535},{0.3412,0.3956},{0.2049,0.2527},{0.0665,0.2668},{0.0013,-0.0840},{0.0727,0.1623},{0.0877,0.0469},{0.1077,-0.0784},{0.0717,-0.2167},{0.2158,-0.2338}};
        createExactShape(starter, 0.5, array, colors, fadeColors,
                trail, flicker, true);
    }


    private static void createExactShape(FireworkParticles.Starter starter,
                                         double speed, double[][] shape, int[] colours, int[] fadeColours, boolean trail, boolean twinkle, boolean creeper) {
        float scale = 1.2f;

        double shapeX = shape[0][0]*scale;
        double shapeY = shape[0][1]*scale;
        Vec3 pos = starter.getPos();
        float randAngle = rand.nextFloat() * 3.1415927F;
        double spreadAngle = creeper ? 0.034 : 0.34;

        for (int i = 0; i < 3; ++i) {
            double angle = randAngle + (i * 3.1415927F) * spreadAngle;
            double d4 = shapeX;
            double d5 = shapeY;

            for (int j = 1; j < shape.length; ++j) {
                double d6 = shape[j][0]*scale;
                double d7 = shape[j][1]*scale;

                float segPerLineOff = 1;
                for (double d8 = segPerLineOff; d8 <= 1.0; d8 += segPerLineOff) {
                    double d9 = Mth.lerp(d8, d4, d6) * speed;
                    double d10 = Mth.lerp(d8, d5, d7) * speed;
                    double d11 = d9 * Math.sin(angle);
                    d9 *= Math.cos(angle);

                    for (double d12 = -1.0; d12 <= 1.0; d12 += 2.0) {
                        starter.createParticle(pos.x, pos.y, pos.z, d9 * d12, d10, d11 * d12, colours, fadeColours, trail, twinkle);
                    }
                }

                d4 = d6;
                d5 = d7;
            }
        }
    }


}
