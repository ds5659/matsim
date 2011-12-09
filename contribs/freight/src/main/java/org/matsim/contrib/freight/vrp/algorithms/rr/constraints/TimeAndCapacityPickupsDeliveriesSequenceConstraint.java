/*******************************************************************************
 * Copyright (C) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * 
 */
package org.matsim.contrib.freight.vrp.algorithms.rr.constraints;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.contrib.freight.vrp.api.Constraints;
import org.matsim.contrib.freight.vrp.api.Costs;
import org.matsim.contrib.freight.vrp.basics.DeliveryFromDepot;
import org.matsim.contrib.freight.vrp.basics.EnRouteDelivery;
import org.matsim.contrib.freight.vrp.basics.EnRoutePickup;
import org.matsim.contrib.freight.vrp.basics.PickupToDepot;
import org.matsim.contrib.freight.vrp.basics.Tour;
import org.matsim.contrib.freight.vrp.basics.TourActivity;
import org.matsim.contrib.freight.vrp.basics.Vehicle;



/**
 * For time dependent vehicle routing, it is assumed that vehicle starts at activity location as early as possible.
 * 
 * @author stefan schroeder
 *
 */
public class TimeAndCapacityPickupsDeliveriesSequenceConstraint implements Constraints {

	private Logger logger = Logger.getLogger(TimeAndCapacityPickupsDeliveriesSequenceConstraint.class);
	
	private int maxCap;
	
	private double maxTime;
	
	private Costs costs;
	
	public TimeAndCapacityPickupsDeliveriesSequenceConstraint(int maxCap, double maxTime, Costs costs) {
		super();
		this.maxCap = maxCap;
		this.costs = costs;
		this.maxTime = maxTime;
	}

	

	@Override
	public boolean judge(Tour tour, Vehicle vehicle) {
		maxCap = vehicle.getCapacity();
		int currentCap = 0;
		boolean deliveryStarted = false;
		Set<String> openCustomers = new HashSet<String>();
		double time = 0.0;
		TourActivity lastAct = null;
		for(TourActivity tourAct : tour.getActivities()){
			if(lastAct == null){
				lastAct = tourAct;
			}
			else{
				time += costs.getTransportTime(lastAct.getLocation(), tourAct.getLocation(), lastAct.getEarliestArrTime() + lastAct.getServiceTime());
			}
			if(time > maxTime){
				return false;
			}
			if(tourAct.getCurrentLoad() > maxCap || tourAct.getCurrentLoad() < 0){
				logger.debug("capacity-conflict (maxCap=" + maxCap + ";currentCap=" + currentCap + " on tour " + tour);
				return false;
			}
			if(tourAct.hasTimeWindowConflict()){
				logger.debug("timeWindow-conflic on tour " + tour);
				return false;
			}
			if(tourAct instanceof EnRoutePickup || tourAct instanceof PickupToDepot){
				if(deliveryStarted){
					if(!openCustomers.isEmpty()){
						return false;
					}
					else{
						deliveryStarted = false;
					}
				}
				openCustomers.add(tourAct.getCustomer().getId());
			}
			if(tourAct instanceof EnRouteDelivery || tourAct instanceof DeliveryFromDepot){
				if(deliveryStarted == false){
					deliveryStarted = true;
				}
				String relatedCustomer = tourAct.getCustomer().getRelation().getCustomer().getId();
				if(openCustomers.contains(relatedCustomer)){
					openCustomers.remove(relatedCustomer);
				}
				else{
					return false;
				}
			}
		}
		return true;
	}

}
