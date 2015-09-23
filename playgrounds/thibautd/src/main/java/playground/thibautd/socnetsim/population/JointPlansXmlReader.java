/* *********************************************************************** *
 * project: org.matsim.*
 * JointPlansXmlReader.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package playground.thibautd.socnetsim.population;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.utils.io.MatsimXmlParser;

import static playground.thibautd.socnetsim.population.JointPlansXmlSchemaNames.JOINT_PLAN_TAG;
import static playground.thibautd.socnetsim.population.JointPlansXmlSchemaNames.PERSON_ATT;
import static playground.thibautd.socnetsim.population.JointPlansXmlSchemaNames.PLAN_NR_ATT;
import static playground.thibautd.socnetsim.population.JointPlansXmlSchemaNames.PLAN_TAG;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.misc.Counter;
import org.xml.sax.Attributes;

/**
 * @author thibautd
 */
public class JointPlansXmlReader extends MatsimXmlParser {
	private final Counter counter = new Counter( "read joint plan #" );

	private final Scenario scenario;
	private final JointPlans jointPlans;

	private Map<Id, Plan> currentJointPlan;

	public JointPlansXmlReader(
			final Scenario scenario) {
		this.scenario = scenario;
		this.jointPlans = new JointPlans();
		scenario.addScenarioElement( JointPlans.ELEMENT_NAME , jointPlans );
	}

	@Override
	public void startTag(
			final String name,
			final Attributes atts,
			final Stack<String> context) {
		if ( name.equals( JOINT_PLAN_TAG ) ) {
			currentJointPlan = new LinkedHashMap<Id, Plan>();
			counter.incCounter();
		}
		if ( name.equals( PLAN_TAG ) ) {
			final Id id = new IdImpl(
						atts.getValue( PERSON_ATT ) );

			final int planIndex = Integer.parseInt(
						atts.getValue( PLAN_NR_ATT ) );

			final Person person = scenario.getPopulation().getPersons().get( id );
			final Plan plan = person.getPlans().get( planIndex );
			// use the ID from person on purpose to minimize memory consumption
			final Plan old = currentJointPlan.put( person.getId() , plan );
			if ( old != null ) throw new RuntimeException( "joint plan contained "+old+" and "+plan+" for agent "+id );
		}
	}

	@Override
	public void endTag(
			final String name,
			final String content,
			final Stack<String> context) {
		if ( name.equals( JOINT_PLAN_TAG ) ) {
			jointPlans.addJointPlan(
					jointPlans.getFactory().createJointPlan(
						currentJointPlan ) );
		}
	}
}
