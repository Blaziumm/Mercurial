package dev.frozenmilk.mercurial.subsystems

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.feature.AllFeatures
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.mercurial.Mercurial
import dev.frozenmilk.util.cell.LazyCell
import java.util.function.Supplier

/**
 * an alternative to [dev.frozenmilk.dairy.core.util.OpModeLazyCell] or [dev.frozenmilk.dairy.core.util.OpModeFreshLazyCell]
 *
 * A [LazyCell] that is invalidated and initialised on the init of an OpMode with [Mercurial] and [subsystem]
 *
 * unlike an [dev.frozenmilk.dairy.core.util.OpModeLazyCell], this is suitable for use in a persistent context,
 * where this shouldn't automatically deregister
 *
 * this can be used in place of an [dev.frozenmilk.dairy.core.util.OpModeFreshLazyCell] when working with [Subsystem]s
 *
 * @see dev.frozenmilk.dairy.core.util.OpModeLazyCell for use in an op mode
 */
class SubsystemObjectCell<T>(val subsystem: Feature, supplier: Supplier<T>) : LazyCell<T>(supplier), Feature {
	override var dependency: Dependency<*> = AllFeatures(Mercurial, subsystem)

	init {
		FeatureRegistrar.registerFeature(this)
	}

	override fun get(): T {
		if (!initialised) {
			if (!FeatureRegistrar.opModeRunning) throw IllegalStateException("Attempted to evaluate contents of SubsystemObjectCell while no opmode active")
			if (!active) throw IllegalStateException("Attempted to evaluate contents of SubsystemObjectCell without Mercurial or required subsystem ($subsystem) attached")
		}
		return super.get()
	}
	override fun postUserInitHook(opMode: Wrapper) {
		safeEvaluate()
		if (opMode.getState() == Wrapper.OpModeState.STOPPED) {
			invalidate()
		}
	}

	override fun cleanup(opMode: Wrapper) {
		invalidate()
	}
}