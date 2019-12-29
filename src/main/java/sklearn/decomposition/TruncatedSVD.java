/*
 * Copyright (c) 2019 Villu Ruusmann
 *
 * This file is part of JPMML-SkLearn
 *
 * JPMML-SkLearn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-SkLearn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-SkLearn.  If not, see <http://www.gnu.org/licenses/>.
 */
package sklearn.decomposition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.dmg.pmml.Apply;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Expression;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMMLFunctions;
import org.jpmml.converter.CMatrixUtil;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.Feature;
import org.jpmml.converter.PMMLUtil;
import org.jpmml.converter.ValueUtil;
import org.jpmml.sklearn.ClassDictUtil;
import org.jpmml.sklearn.SkLearnEncoder;

public class TruncatedSVD extends BasePCA {

	public TruncatedSVD(String module, String name){
		super(module, name);
	}

	@Override
	public List<Feature> encodeFeatures(List<Feature> features, SkLearnEncoder encoder){
		int[] shape = getComponentsShape();

		int numberOfComponents = shape[0];
		int numberOfFeatures = shape[1];

		List<? extends Number> components = getComponents();

		ClassDictUtil.checkSize(numberOfFeatures, features);

		String id = "svd@" + String.valueOf(TruncatedSVD.SEQUENCE.getAndIncrement());

		List<Feature> result = new ArrayList<>();

		for(int i = 0; i < numberOfComponents; i++){
			List<? extends Number> component = CMatrixUtil.getRow(components, numberOfComponents, numberOfFeatures, i);

			Apply apply = new Apply(PMMLFunctions.SUM);

			for(int j = 0; j < numberOfFeatures; j++){
				Feature feature = features.get(j);

				Number componentValue = component.get(j);

				if(ValueUtil.isOne(componentValue)){
					apply.addExpressions(feature.ref());

					continue;
				}

				ContinuousFeature continuousFeature = feature.toContinuousFeature();

				// "$name[i] * component[i]"
				Expression expression = PMMLUtil.createApply(PMMLFunctions.MULTIPLY, continuousFeature.ref(), PMMLUtil.createConstant(componentValue));

				apply.addExpressions(expression);
			}

			DerivedField derivedField = encoder.createDerivedField(FieldName.create(id + "[" + String.valueOf(i) + "]"), apply);

			result.add(new ContinuousFeature(encoder, derivedField));
		}

		return result;
	}

	private static final AtomicInteger SEQUENCE = new AtomicInteger(1);
}