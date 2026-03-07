package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.ConflictChain;

@ConflictChain(value = MirageMirror.class, node = DarkMatterSheath.class)
public class EclipseMirror extends BaseCurio {
    protected EclipseMirror(Properties properties) {
        super(properties);
    }
}
