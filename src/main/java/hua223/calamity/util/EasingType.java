package hua223.calamity.util;

import net.minecraft.util.Mth;

public enum EasingType {
    // 多项式·加速入：起步缓慢、越到后面冲得越猛（慢→快）。degree 越大，前段越平、结尾越陡。
    POLY_IN {
        @Override
        public float easingFunction(float amount, int degree) {
            return (float) Math.pow(amount, degree);
        }
    },

    // 多项式·减速出：起步冲刺、越到后面越平缓地收尾（快→慢）。常用于顺滑地抵达目标值。
    POLY_OUT {
        @Override
        public float easingFunction(float amount, int degree) {
            return 1f - (float) Math.pow(1f - amount, degree);
        }
    },

    // 多项式·S形：前后对称，慢起步→中段提速→慢收尾（慢→快→慢）。degree 控制两端停顿的强弱。
    POLY_IN_OUT {
        @Override
        public float easingFunction(float amount, int degree) {
            return amount < 0.5f ? (float) Math.pow(2, degree - 1) *
                (float) Math.pow(amount, degree) : 1f - (float) Math.pow(-2 * amount + 2, degree) / 2f;
        }
    },

    // 正弦·加速入：柔和版的慢→快，起步比多项式更圆滑，无生硬感。
    SINE_IN {
        @Override
        public float easingFunction(float amount, int degree) {
            return 1f - (float) Math.cos(amount * Mth.PI / 2f);
        }
    },

    // 正弦·减速出：柔和版的快→慢，末尾以近乎静止的方式轻缓落位。
    SINE_OUT {
        @Override
        public float easingFunction(float amount, int degree) {
            return (float) Math.sin(amount * Mth.PI / 2f);
        }
    },

    // 正弦·S形：整条曲线如半个正弦波般平滑，慢→快→慢，加减速过渡最自然。
    SINE_IN_OUT {
        @Override
        public float easingFunction(float amount, int degree) {
            return -((float) Math.cos(amount * Mth.PI) - 1) / 2f;
        }
    },

    // 正弦·凸起脉冲：从 0 起、在中点冲到峰值 1、再回落至 0，形成一次来回的鼓点节奏（0→1→0）。
    SINE_BUMP {
        @Override
        public float easingFunction(float amount, int degree) {
            return (float) Math.sin(amount * Mth.PI);
        }
    },

    // 指数·加速入：极端的慢→快，前段几乎不动、结尾瞬间爆发，节奏张力最强。
    EXP_IN {
        @Override
        public float easingFunction(float amount, int degree) {
            return amount == 0f ? 0f : (float) Math.pow(2, 10f * amount - 10f);
        }
    },

    // 指数·减速出：极端的快→慢，开头猛冲后用指数衰减长长地滑向终点。
    EXP_OUT {
        @Override
        public float easingFunction(float amount, int degree) {
            return amount == 1f ? 1f : 1f - (float) Math.pow(2, -10f * amount);
        }
    },

    // 指数·S形：两头都极端（慢起步→中段暴冲→慢收尾），比 POLY/SINE_IN_OUT 更戏剧化的中间加速。
    EXP_IN_OUT {
        @Override
        public float easingFunction(float amount, int degree) {
            return amount == 1f ? 1f : amount < 0.5f ? (float) Math.pow(2, 20f * amount - 10f) / 2f : (2f - (float) Math.pow(2, -20f * amount - 10f)) / 2f;
        }
    },

    // 圆形·加速入：沿圆弧运动，起步比多项式更顿、结尾冲刺更快（慢→快），带机械般的紧致感。
    CIRC_IN {
        @Override
        public float easingFunction(float amount, int degree) {
            return 1f - (float) Math.sqrt(1 - Math.pow(amount, 2f));
        }
    },

    // 圆形·减速出：圆弧的逆过程，开头迅速起跳后沿弧线缓缓贴合终点（快→慢）。
    CIRC_OUT {
        @Override
        public float easingFunction(float amount, int degree) {
            return (float) Math.sqrt(1 - Math.pow(amount - 1f, 2f));
        }
    },

    // 圆形·S形：两段圆弧拼接的慢→快→慢，中段切换处速度变化比正弦更硬朗。
    CIRC_IN_OUT {
        @Override
        public float easingFunction(float amount, int degree) {
            return amount < 0.5 ? (1f - (float)Math.sqrt(1 - Math.pow(2 * amount, 2f))) / 2f : ((float)Math.sqrt(1 - Math.pow(-2f * amount + 2f, 2f)) + 1f) / 2f;
        }
    },

    // 线性·匀速：无加减速，从头到尾恒定速度变化（amount 直接作为输出）。
    LINEAR {
        @Override
        public float easingFunction(float amount, int degree) {
            return amount;
        }
    };

    public abstract float easingFunction(float amount, int degree);
}
