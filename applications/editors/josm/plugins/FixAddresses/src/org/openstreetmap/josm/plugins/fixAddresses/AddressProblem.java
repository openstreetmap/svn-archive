// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * Address problem.
 */
public class AddressProblem implements IProblem {
    private List<ISolution> solutions = null;
    private String description;
    private ProblemType type;
    private IOSMEntity source;

    /**
     * Instantiates a new problem.
     *
     * @param source the source
     * @param description The problem description.
     * @param solutions This list of solutions.
     * @param type the type
     */
    public AddressProblem(IOSMEntity source, String description,
            List<ISolution> solutions, ProblemType type) {
        super();
        this.source = source;
        this.description = description;
        this.solutions = solutions;
        this.type = type;
    }

    /**
     * Instantiates a new problem with type 'warning'.
     *
     * @param source the source
     * @param description The problem description.
     * @param solutions This list of solutions.
     */
    public AddressProblem(IOSMEntity source, String description, List<ISolution> solutions) {
        this(source, description, solutions, ProblemType.Warning);
    }

    /**
     * Instantiates a new problem with type 'warning' and without solutions.
     *
     * @param source the source
     * @param description The problem description.
     */
    public AddressProblem(IOSMEntity source, String description) {
        this(source, description, null, ProblemType.Warning);
    }

    /**
     * Creates the solution list, if necessary.
     */
    private void lazyCreateSolutions() {
        if (solutions == null) {
            solutions = new ArrayList<>();
        }
    }

    @Override
    public void addSolution(ISolution solution) {
        CheckParameterUtil.ensureParameterNotNull(solution, "solution");

        lazyCreateSolutions();
        solutions.add(solution);
    }

    @Override
    public void applySolution(ISolution solution) {
        CheckParameterUtil.ensureParameterNotNull(solution, "solution");

        solution.solve();
    }

    @Override
    public void clearSolutions() {
        if (solutions == null) return;

        solutions.clear();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<ISolution> getSolutions() {
        return solutions;
    }

    @Override
    public ProblemType getType() {
        return type;
    }

    @Override
    public void removeSolution(ISolution solution) {
        if (solutions == null) throw new RuntimeException("Solution list is null");
        if (solutions.size() == 0) throw new RuntimeException("Solution list is empty");

        CheckParameterUtil.ensureParameterNotNull(solution, "solution");
        solutions.remove(solution);
    }

    @Override
    public IOSMEntity getSource() {
        return source;
    }
}
