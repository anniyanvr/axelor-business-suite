/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.sale.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.SaleOrderLineTax;
import com.axelor.apps.sale.db.SaleOrderSubLine;
import com.google.inject.Inject;

public class SaleOrderLineTaxService {

	private static final Logger LOG = LoggerFactory.getLogger(SaleOrderLineTaxService.class); 
	
	@Inject
	private SaleOrderToolService saleOrderToolService;
	
	
	
	
	/**
	 * Créer les lignes de TVA du devis. La création des lignes de TVA se
	 * basent sur les lignes de devis ainsi que les sous-lignes de devis de
	 * celles-ci.
	 * Si une ligne de devis comporte des sous-lignes de devis, alors on se base uniquement sur celles-ci.
	 * 
	 * @param invoice
	 *            La facture.
	 * 
	 * @param invoiceLines
	 *            Les lignes de facture.
	 * 
	 * @param invoiceLineTaxes
	 *            Les lignes des taxes de la facture.
	 * 
	 * @return La liste des lignes de taxe de la facture.
	 */
	public List<SaleOrderLineTax> createsSaleOrderLineTax(SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList) {
		
		List<SaleOrderLineTax> saleOrderLineTaxList = new ArrayList<SaleOrderLineTax>();
		Map<TaxLine, SaleOrderLineTax> map = new HashMap<TaxLine, SaleOrderLineTax>();
		
		if (saleOrderLineList != null && !saleOrderLineList.isEmpty()) {

			LOG.debug("Création des lignes de tva pour les lignes de factures.");
			
			for (SaleOrderLine saleOrderLine : saleOrderLineList) {
				
				if(saleOrderLine.getSaleOrderSubLineList() != null && !saleOrderLine.getSaleOrderSubLineList().isEmpty())  {
					
					for(SaleOrderSubLine saleOrderSubLine : saleOrderLine.getSaleOrderSubLineList())  {
						TaxLine taxLine = saleOrderSubLine.getTaxLine();
						LOG.debug("Tax {}", taxLine);
						
						if (map.containsKey(taxLine)) {
						
							SaleOrderLineTax saleOrderLineTax = map.get(taxLine);
							
							saleOrderLineTax.setExTaxBase(saleOrderLineTax.getExTaxBase().add(saleOrderSubLine.getExTaxTotal()));
							
						}
						else {
							
							SaleOrderLineTax saleOrderLineTax = new SaleOrderLineTax();
							saleOrderLineTax.setSaleOrder(saleOrder);
							
							saleOrderLineTax.setExTaxBase(saleOrderSubLine.getExTaxTotal());
							
							saleOrderLineTax.setTaxLine(taxLine);
							map.put(taxLine, saleOrderLineTax);
							
						}
					}
				}
				else  {
				
					TaxLine taxLine = saleOrderLine.getTaxLine();
					LOG.debug("Tax {}", taxLine);
					
					if (map.containsKey(taxLine)) {
					
						SaleOrderLineTax saleOrderLineTax = map.get(taxLine);
						
						saleOrderLineTax.setExTaxBase(saleOrderLineTax.getExTaxBase().add(saleOrderLine.getExTaxTotal()));
						
					}
					else {
						
						SaleOrderLineTax saleOrderLineTax = new SaleOrderLineTax();
						saleOrderLineTax.setSaleOrder(saleOrder);
						
						saleOrderLineTax.setExTaxBase(saleOrderLine.getExTaxTotal());
						
						saleOrderLineTax.setTaxLine(taxLine);
						map.put(taxLine, saleOrderLineTax);
						
					}
				}
			}
		}
			
		for (SaleOrderLineTax saleOrderLineTax : map.values()) {
			
			// Dans la devise de la facture
			BigDecimal exTaxBase = saleOrderLineTax.getExTaxBase();
			BigDecimal taxTotal = saleOrderToolService.computeAmount(exTaxBase, saleOrderLineTax.getTaxLine().getValue());
			saleOrderLineTax.setTaxTotal(taxTotal);
			saleOrderLineTax.setInTaxTotal(exTaxBase.add(taxTotal));
			
			saleOrderLineTaxList.add(saleOrderLineTax);

			LOG.debug("Ligne de TVA : Total TVA => {}, Total HT => {}", new Object[] {saleOrderLineTax.getTaxTotal(), saleOrderLineTax.getInTaxTotal()});
			
		}

		return saleOrderLineTaxList;
	}

	
	
}
