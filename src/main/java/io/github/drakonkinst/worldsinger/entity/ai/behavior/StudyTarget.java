package io.github.drakonkinst.worldsinger.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.DelayedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

public class StudyTarget<E extends MobEntity> extends DelayedBehaviour<E> {

    @Nullable
    protected LivingEntity target = null;

    protected BiPredicate<E, LivingEntity> canStudyPredicate;

    private static final List<Pair<MemoryModuleType<?>, MemoryModuleState>> MEMORY_REQUIREMENTS = ObjectArrayList.of(
            Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT),
            Pair.of(MemoryModuleType.UNIVERSAL_ANGER, MemoryModuleState.VALUE_ABSENT),
            Pair.of(MemoryModuleType.HURT_BY, MemoryModuleState.VALUE_ABSENT));

    public StudyTarget(int studyTicks) {
        super(studyTicks);
    }

    public StudyTarget<E> canStudy(BiPredicate<E, LivingEntity> predicate) {
        this.canStudyPredicate = predicate;
        return this;
    }

    @Override
    protected boolean shouldRun(ServerWorld level, E entity) {
        this.target = BrainUtils.getTargetOfEntity(entity);

        return this.canStudyPredicate.test(entity, target) && entity.getVisibilityCache()
                .canSee(this.target) && entity.isInAttackRange(this.target);
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        // Cancel running if attacked
        return super.shouldKeepRunning(entity) && !BrainUtils.hasMemory(entity,
                MemoryModuleType.HURT_BY);
    }

    @Override
    protected void start(E entity) {
        LookTargetUtil.lookAt(entity, this.target);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryModuleState>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }
}
