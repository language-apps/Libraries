package acorns.mp3.encoder;

/**
 * Global Type Definitions
 * 
 * @author Ken
 * 
 */
public interface IIterationLoop {
	void iteration_loop(final LameGlobalFlags gfp, float pe[][],
			float ms_ratio[], III_psy_ratio ratio[][]);
}