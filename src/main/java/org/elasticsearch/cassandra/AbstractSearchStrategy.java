package org.elasticsearch.cassandra;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cassandra.dht.LongToken;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import com.google.common.collect.ImmutableList;

/**
 * Only support Murmur3 Long Token.
 * @author vroyer
 *
 */
public abstract class AbstractSearchStrategy {
	private static ESLogger logger = Loggers.getLogger(AbstractSearchStrategy.class);
	
	public static final Collection<Range<Token>> EMPTY_RANGE_TOKEN = ImmutableList.<Range<Token>>of();
	
	public abstract AbstractSearchStrategy.Result topology(String ksName);
	
	
	public class Result {
		Map<Range<Token>,List<InetAddress>> rangesMap;
		Map<InetAddress,Collection<Range<Token>>> searchTopology;
		Set<InetAddress> unreachableTokenOwners;
		Set<Range<Token>> orphanRanges;
		
		public Result(Map<InetAddress,Collection<Range<Token>>> searchTopology, Set<Range<Token>> orphanRanges, Set<InetAddress> unreachableTokenOwners, Map<Range<Token>,List<InetAddress>> rangesMap) {
			this.unreachableTokenOwners = unreachableTokenOwners;
			this.searchTopology = searchTopology;
			this.orphanRanges = orphanRanges;
			this.rangesMap = rangesMap;
			sortAndMerge();
		}
		
		public Map<InetAddress,Collection<Range<Token>>> getTopology() {
			return this.searchTopology;
		}
		
		public Collection<Range<Token>> getTokenRanges(InetAddress addr) {
			return this.searchTopology.get(addr);
		}
		
		public Set<Range<Token>> getOrphanTokenRanges() {
			return this.orphanRanges;
		}
		
		
		public Set<InetAddress> getUnreachableTokenOwners() {
			return unreachableTokenOwners;
		}
		
		public boolean isConsistent() {
			return orphanRanges.size() == 0;
		}
		
		public Map<Range<Token>,List<InetAddress>> getRangeToAddressMap() {
			return rangesMap;
		}
		
		/**
		 * Merge contiguous token ranges.
		 */
		public void sortAndMerge() {
			Map<InetAddress,Collection<Range<Token>>> optimizedTopology = new HashMap<InetAddress,Collection<Range<Token>>>();
			for(InetAddress addr : searchTopology.keySet()) {
				optimizedTopology.put(addr, sortAndMergeRange( searchTopology.get(addr) ) );
			}
			this.searchTopology = optimizedTopology;
		}
		
		public Collection<Range<Token>> sortAndMergeRange( Collection<Range<Token>> ranges) {
			ArrayList<Range<Token>> optimizedRange = new ArrayList<Range<Token>>();
			for(Range<Token> range : ranges) {
				if (range.isWrapAround()) {
					optimizedRange.add(new Range<Token>(
							new LongToken((Long)range.left.getTokenValue()),
							new LongToken(Long.MAX_VALUE)));
					optimizedRange.add(new Range<Token>(
							new LongToken(Long.MIN_VALUE),
							new LongToken((Long)range.right.getTokenValue())));
				} else {
					optimizedRange.add(range);
				}
			}
			Collections.sort(optimizedRange);
			logger.debug(" sort in={} out={}", ranges, optimizedRange);
			int i = 0;
			while (i < optimizedRange.size() - 1) {
				Range<Token> range1 = optimizedRange.get(i);
				Range<Token> range2 = optimizedRange.get(i+1);
				if (range1.right.equals( range2.left )) {
					optimizedRange.set(i+1, new Range<Token>(range1.left,range2.right));
					optimizedRange.remove(i);
				} else {
					i++;
				}
			}
			logger.debug(" merge out={}", optimizedRange);
			return optimizedRange;
		}
		
	}

}
