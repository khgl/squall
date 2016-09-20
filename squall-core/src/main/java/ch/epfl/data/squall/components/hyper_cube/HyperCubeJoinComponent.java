/*
 * Copyright (c) 2011-2015 EPFL DATA Laboratory
 * Copyright (c) 2014-2015 The Squall Collaboration (see NOTICE)
 *
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.epfl.data.squall.components.hyper_cube;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.epfl.data.squall.storm_components.StormEmitter;
import ch.epfl.data.squall.storm_components.hyper_cube.StormHyperCubeJoin;
import ch.epfl.data.squall.storm_components.hyper_cube.TraditionalStormHyperCubeJoin;
import ch.epfl.data.squall.thetajoin.matrix_assignment.ManualHybridHyperCubeAssignment.Dimension;
import ch.epfl.data.squall.storm_components.theta.StormThetaJoin;
import ch.epfl.data.squall.types.Type;
import org.apache.log4j.Logger;

import backtype.storm.Config;
import backtype.storm.topology.TopologyBuilder;
import ch.epfl.data.squall.predicates.Predicate;
import ch.epfl.data.squall.components.Component;
import ch.epfl.data.squall.components.AbstractJoinerComponent;
import ch.epfl.data.squall.storm_components.synchronization.TopologyKiller;
import ch.epfl.data.squall.utilities.MyUtilities;

public class HyperCubeJoinComponent extends AbstractJoinerComponent<HyperCubeJoinComponent> {
    protected HyperCubeJoinComponent getThis() {
      return this;
    }

    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(HyperCubeJoinComponent.class);
    private Type contentSensitiveThetaJoinWrapper = null;
    private Map<String, Predicate> joinPredicate = null;

    private Map<String, String[]> _relColNames;
    private Set<String> _randomColumns;
    private Map<String, Type[]> _parentNameColTypes;

    public HyperCubeJoinComponent(Component[] parents,
        Map<String, Predicate> joinPredicate, Map<String, Type[]> relationTypes, 
      Map<String, String[]> relColNames, Set<String> randomColumns) {
      super(parents);

      _parentNameColTypes = relationTypes;
      _relColNames = relColNames;
      _randomColumns = randomColumns;

      this.joinPredicate = joinPredicate;
    }

    @Override
    public List<String> getFullHashList() {
        throw new RuntimeException(
                "Load balancing for Theta join is done inherently!");
    }

    @Override
    public void makeBolts(TopologyBuilder builder, TopologyKiller killer,
                          List<String> allCompNames, Config conf, int hierarchyPosition) {
        MyUtilities.checkBatchOutput(getBatchOutputMillis(),
                                     getChainOperator().getAggregation(), conf);
        ArrayList<StormEmitter> emitters = new ArrayList<StormEmitter>();
        for (StormEmitter se : getParents())
            emitters.add(se);

        
        setStormEmitter(new TraditionalStormHyperCubeJoin(emitters, _relColNames, 
            _parentNameColTypes, _randomColumns, this, allCompNames, joinPredicate,
            hierarchyPosition, builder, killer, conf, contentSensitiveThetaJoinWrapper));
    }

    // list of distinct keys, used for direct stream grouping and load-balancing
    // ()
    @Override
    public HyperCubeJoinComponent setFullHashList(List<String> fullHashList) {
        throw new RuntimeException(
                "Load balancing for Theta join is done inherently!");
    }

    @Override
    public HyperCubeJoinComponent setContentSensitiveThetaJoinWrapper(
            Type wrapper) {
        contentSensitiveThetaJoinWrapper = wrapper;
        return this;
    }
}
