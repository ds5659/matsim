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
package org.matsim.contrib.freight.vrp.algorithms.rr.basics;

import org.matsim.contrib.freight.vrp.algorithms.rr.api.TourActivityStatusUpdater;
import org.matsim.contrib.freight.vrp.api.Costs;
import org.matsim.contrib.freight.vrp.basics.DeliveryFromDepot;
import org.matsim.contrib.freight.vrp.basics.Tour;
import org.matsim.contrib.freight.vrp.basics.TourActivity;


/**
 * It does not consider time dependent travel times. It does not make that much sense to 
 * enable this with it, since if start time does not matter, one should conduct the tour at 
 * night where we have no traffic. If start time matters, we have can formulate a vrp-problem 
 * with time windows.
 * 
 * @author stefan schroeder
 *
 */

public class TourActivityStatusUpdaterImpl implements TourActivityStatusUpdater{
	
	private Costs costs;
	
	public TourActivityStatusUpdaterImpl(Costs costs) {
		super();
		this.costs = costs;
	}
	
	@Override
	public void update(Tour tour){
		reset(tour);
		double cost = 0.0;
		int loadAtDepot = getLoadAtDepot(tour);
		tour.getActivities().get(0).setCurrentLoad(loadAtDepot);
		for(int i=1;i<tour.getActivities().size();i++){
			TourActivity fromAct = tour.getActivities().get(i-1);
			TourActivity toAct = tour.getActivities().get(i);
			cost += costs.getGeneralizedCost(fromAct.getLocation(),toAct.getLocation(), 0.0);
			tour.costs.generalizedCosts += costs.getGeneralizedCost(fromAct.getLocation(),toAct.getLocation(), 0.0);
			tour.costs.distance += costs.getDistance(fromAct.getLocation(),toAct.getLocation(), 0.0);
			tour.costs.time  += costs.getTransportTime(fromAct.getLocation(),toAct.getLocation(), 0.0);
			int loadAtCustomer = fromAct.getCurrentLoad() + toAct.getCustomer().getDemand();
			toAct.setCurrentLoad(loadAtCustomer);
		}
	}
	
	private void reset(Tour tour) {
		tour.costs.generalizedCosts = 0.0;
		tour.costs.time = 0.0;
		tour.costs.distance = 0.0;
	}

	private int getLoadAtDepot(Tour tour) {
		int loadAtDepot = 0;
		for(TourActivity tA : tour.getActivities()){
			if(tA instanceof DeliveryFromDepot){
				loadAtDepot += tA.getCustomer().getDemand();
			}
		}
		return loadAtDepot*-1;
	}

}
