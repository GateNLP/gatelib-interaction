/*
 * Copyright (c) 2018-2019 The University of Sheffield.
 *
 * This file is part of gatelib-interaction 
 * (see https://github.com/GateNLP/gatelib-interaction).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package gate.lib.interaction.data;

import java.io.Serializable;

/**
 * Minimal container for a sparse vector of n non-zero locations.
 * 
 * This is meant to be used only for passing on sparse vectors, so 
 * no attempt is made to have any of the specific methods normally
 * needed for sparse vectors, and especially there is no code to make
 * accessing vector elements fast.
 * 
 * @author Johann Petrak
 */
public class SparseDoubleVector implements Serializable {

  private static final long serialVersionUID = 2L;
 
  protected int[] indices;
  protected double[] values;
  protected double instanceWeight = Double.NaN;
  
  /**
   * Create a sparse vector with the specified number of non-zero locations.
   * 
   * @param numberOfLocations the number of non-zero locations
   */
  public SparseDoubleVector(int numberOfLocations) {
    indices = new int[numberOfLocations];
    values = new double[numberOfLocations];
  }
  
  /**
   * Return the indices of non-zero locations in the sparse vector. 
   * 
   * @return int array of indices
   */
  public int[] getLocations() { return indices; }
  /**
   * Return the values of non-zero locations in the sparse vector.
   * 
   *
   * @return double array of values
   */
  public double[] getValues() { return values; }
  /**
   * Get the number of non-zero locations in the sparse vector.
   * 
   * @return number of non-zero locations
   */
  public int nLocations() { return indices.length; }
  /**
   * Return a weight associated with the whole vector.
   * @return the weight
   */
  public double getInstanceWeight () { return instanceWeight; }
  /**
   * Set a weight associated with the whole vector.
   * @param weight the weight
   */
  public void setInstanceWeight(double weight) { instanceWeight = weight; }
  
}
