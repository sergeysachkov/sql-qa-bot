#!/bin/bash

 . ./path.sh || exit 1
 . ./cmd.sh || exit 1
 
 nj=1       # number of parallel jobs - 1 is perfect for such a small data set
 njobs=1
 lm_order=1 # language model order (n-gram quantity) - 1 is enough for digits grammar
 
# Safety mechanism (possible running this script with modified arguments)
 . utils/parse_options.sh || exit 1
 [[ $# -ge 1 ]] && { echo "Wrong arguments!"; exit 1; }
 
 # Removing previously created data (from last run.sh execution)
 rm -rf exp mfcc data/train/spk2utt data/train/cmvn.scp data/train/feats.scp data/train/split1 data/test/spk2utt data/test/cmvn.scp data/test/feats.scp data/test/split1 data/local/lang data/lang data/local/tmp data/local/dict/lexiconp.txt
 
 echo
 echo "===== PREPARING ACOUSTIC DATA ====="
 echo
 
 # Needs to be prepared by hand (or using self written scripts):
 #
 # spk2gender  [<speaker-id> <gender>]
 # wav.scp     [<uterranceID> <full_path_to_audio_file>]
 # text           [<uterranceID> <text_transcription>]
 # utt2spk     [<uterranceID> <speakerID>]
 # corpus.txt  [<text_transcription>]
 
 # Making spk2utt files
 utils/utt2spk_to_spk2utt.pl data/train/utt2spk > data/train/spk2utt
 utils/utt2spk_to_spk2utt.pl data/test/utt2spk > data/test/spk2utt
 
 echo
 echo "===== FEATURES EXTRACTION ====="
 echo
 
 # Making feats.scp files
 mfccdir=mfcc
 # Uncomment and modify arguments in scripts below if you have any problems with data sorting
 # utils/validate_data_dir.sh data/train     # script for checking prepared data - here: for data/train directory
 # utils/fix_data_dir.sh data/train          # tool for data proper sorting if needed - here: for data/train directory
 steps/make_mfcc.sh --nj $nj --cmd "$train_cmd" data/train exp/make_mfcc/train $mfccdir
 steps/make_mfcc.sh --nj $nj --cmd "$train_cmd" data/test exp/make_mfcc/test $mfccdir
 
 # Making cmvn.scp files
 steps/compute_cmvn_stats.sh data/train exp/make_mfcc/train $mfccdir
 steps/compute_cmvn_stats.sh data/test exp/make_mfcc/test $mfccdir
 
 echo
 echo "===== PREPARING LANGUAGE DATA ====="
 echo
 
 # Needs to be prepared by hand (or using self written scripts):
 #
 # lexicon.txt           [<word> <phone 1> <phone 2> ...]
 # nonsilence_phones.txt    [<phone>]
 # silence_phones.txt    [<phone>]
 # optional_silence.txt  [<phone>]
 
 # Preparing language data
 utils/prepare_lang.sh data/local/dict "<UNK>" data/local/lang data/lang
 
 echo
 echo "===== LANGUAGE MODEL CREATION ====="
 echo "===== MAKING lm.arpa ====="
 echo
 
 loc=`which ngram-count`;
 if [ -z $loc ]; then
    if uname -a | grep 64 >/dev/null; then
            sdir=$KALDI_ROOT/tools/srilm/bin/i686-m64
    else
                    sdir=$KALDI_ROOT/tools/srilm/bin/i686
    fi
    if [ -f $sdir/ngram-count ]; then
                    echo "Using SRILM language modelling tool from $sdir"
                    export PATH=$PATH:$sdir
    else
                    echo "SRILM toolkit is probably not installed.
                            Instructions: tools/install_srilm.sh"
                    exit 1
    fi
 fi
 
 local=data/local
 mkdir $local/tmp
 ngram-count -order $lm_order -write-vocab $local/tmp/vocab-full.txt -wbdiscount -text $local/corpus.txt -lm $local/lm.arpa
 
 
test=data/lang_test
mkdir -p $test
for f in phones.txt words.txt phones.txt L.fst L_disambig.fst phones; do
    cp -r data/lang/$f $test
done
cat $local/lm.arpa | \
  arpa2fst --disambig-symbol=#0 \
           --read-symbol-table=$test/words.txt - $test/G.fst
fstisstochastic $test/G.fst

# Train monophone models on a subset of the data
utils/subset_data_dir.sh data/train 54 data/train.1k  || exit 1;
steps/train_mono.sh --nj $nj --cmd "$train_cmd" data/train.1k data/lang exp/mono  || exit 1

# Monophone decoding
utils/mkgraph.sh data/lang_test exp/mono exp/mono/graph || exit 1
# note: local/decode.sh calls the command line once for each
# test, and afterwards averages the WERs into (in this case
# exp/mono/decode/
steps/decode.sh --config conf/decode.config --nj $njobs --cmd "$decode_cmd" \
  exp/mono/graph data/test exp/mono/decode

# Get alignments from monophone system.
steps/align_si.sh --nj $njobs --cmd "$train_cmd" \
  data/train data/lang exp/mono exp/mono_ali || exit 1;

# train tri1 [first triphone pass]
steps/train_deltas.sh --cmd "$train_cmd" \
  2000 11000 data/train data/lang exp/mono_ali exp/tri1 || exit 1;

# decode tri1
utils/mkgraph.sh data/lang_test exp/tri1 exp/tri1/graph || exit 1;
steps/decode.sh --config conf/decode.config --nj $njobs --cmd "$decode_cmd" \
  exp/tri1/graph data/test exp/tri1/decode

#draw-tree data/lang/phones.txt exp/tri1/tree | dot -Tps -Gsize=8,10.5 | ps2pdf - tree.pdf

# align tri1
steps/align_si.sh --nj $njobs --cmd "$train_cmd" \
  --use-graphs true data/train data/lang exp/tri1 exp/tri1_ali || exit 1;

# train tri2a [delta+delta-deltas]
steps/train_deltas.sh --cmd "$train_cmd" 2000 11000 \
  data/train data/lang exp/tri1_ali exp/tri2a || exit 1;

# decode tri2a
utils/mkgraph.sh data/lang_test exp/tri2a exp/tri2a/graph
steps/decode.sh --config conf/decode.config --nj $njobs --cmd "$decode_cmd" \
  exp/tri2a/graph data/test exp/tri2a/decode

# train and decode tri2b [LDA+MLLT]
steps/train_lda_mllt.sh --cmd "$train_cmd" 2000 11000 \
  data/train data/lang exp/tri1_ali exp/tri2b || exit 1;
utils/mkgraph.sh data/lang_test exp/tri2b exp/tri2b/graph
steps/decode.sh --config conf/decode.config --nj $njobs --cmd "$decode_cmd" \
  exp/tri2b/graph data/test exp/tri2b/decode

# Align all data with LDA+MLLT system (tri2b)
steps/align_si.sh --nj $njobs --cmd "$train_cmd" --use-graphs true \
   data/train data/lang exp/tri2b exp/tri2b_ali || exit 1;

#  Do MMI on top of LDA+MLLT.
steps/make_denlats.sh --nj $njobs --cmd "$train_cmd" \
  data/train data/lang exp/tri2b exp/tri2b_denlats || exit 1;
steps/train_mmi.sh data/train data/lang exp/tri2b_ali exp/tri2b_denlats exp/tri2b_mmi || exit 1;
steps/decode.sh --config conf/decode.config --iter 4 --nj $njobs --cmd "$decode_cmd" \
   exp/tri2b/graph data/test exp/tri2b_mmi/decode_it4
steps/decode.sh --config conf/decode.config --iter 3 --nj $njobs --cmd "$decode_cmd" \
   exp/tri2b/graph data/test exp/tri2b_mmi/decode_it3

# Do the same with boosting.
steps/train_mmi.sh --boost 0.05 data/train data/lang \
   exp/tri2b_ali exp/tri2b_denlats exp/tri2b_mmi_b0.05 || exit 1;
steps/decode.sh --config conf/decode.config --iter 4 --nj $njobs --cmd "$decode_cmd" \
   exp/tri2b/graph data/test exp/tri2b_mmi_b0.05/decode_it4 || exit 1;
steps/decode.sh --config conf/decode.config --iter 3 --nj $njobs --cmd "$decode_cmd" \
   exp/tri2b/graph data/test exp/tri2b_mmi_b0.05/decode_it3 || exit 1;

# Do MPE.
steps/train_mpe.sh data/train data/lang exp/tri2b_ali exp/tri2b_denlats exp/tri2b_mpe;
steps/decode.sh --config conf/decode.config --iter 4 --nj $njobs --cmd "$decode_cmd" \
   exp/tri2b/graph data/test exp/tri2b_mpe/decode_it4;
steps/decode.sh --config conf/decode.config --iter 3 --nj $njobs --cmd "$decode_cmd" \
   exp/tri2b/graph data/test exp/tri2b_mpe/decode_it3;


## Do LDA+MLLT+SAT, and decode.
steps/train_sat.sh 2000 11000 data/train data/lang exp/tri2b_ali exp/tri3b || exit 1;
utils/mkgraph.sh data/lang_test exp/tri3b exp/tri3b/graph || exit 1;
steps/decode_fmllr.sh --config conf/decode.config --nj $njobs --cmd "$decode_cmd" \
  exp/tri3b/graph data/test exp/tri3b/decode || exit 1;


# Align all data with LDA+MLLT+SAT system (tri3b)
steps/align_fmllr.sh --nj $njobs --cmd "$train_cmd" --use-graphs true \
  data/train data/lang exp/tri3b exp/tri3b_ali || exit 1;

## MMI on top of tri3b (i.e. LDA+MLLT+SAT+MMI)
steps/make_denlats.sh --config conf/decode.config \
   --nj $njobs --cmd "$train_cmd" --transform-dir exp/tri3b_ali \
  data/train data/lang exp/tri3b exp/tri3b_denlats || exit 1;
steps/train_mmi.sh data/train data/lang exp/tri3b_ali exp/tri3b_denlats exp/tri3b_mmi || exit 1;

steps/decode_fmllr.sh --config conf/decode.config --nj $njobs --cmd "$decode_cmd" \
  --alignment-model exp/tri3b/final.alimdl --adapt-model exp/tri3b/final.mdl \
   exp/tri3b/graph data/test exp/tri3b_mmi/decode || exit 1;

# Do a decoding that uses the exp/tri3b/decode directory to get transforms from.
steps/decode.sh --config conf/decode.config --nj $njobs --cmd "$decode_cmd" \
  --transform-dir exp/tri3b/decode  exp/tri3b/graph data/test exp/tri3b_mmi/decode2 || exit 1;


#first, train UBM for fMMI experiments.
steps/train_diag_ubm.sh --silence-weight 0.5 --nj $njobs --cmd "$train_cmd" \
  250 data/train data/lang exp/tri3b_ali exp/dubm3b

# Next, various fMMI+MMI configurations.
steps/train_mmi_fmmi.sh --learning-rate 0.0025 \
  --boost 0.1 --cmd "$train_cmd" data/train data/lang exp/tri3b_ali exp/dubm3b exp/tri3b_denlats \
  exp/tri3b_fmmi_b || exit 1;

for iter in 3 4 5 6 7 8; do
 steps/decode_fmmi.sh --nj $njobs --config conf/decode.config --cmd "$decode_cmd" --iter $iter \
   --transform-dir exp/tri3b/decode  exp/tri3b/graph data/test exp/tri3b_fmmi_b/decode_it$iter &
done

steps/train_mmi_fmmi.sh --learning-rate 0.001 \
  --boost 0.1 --cmd "$train_cmd" data/train data/lang exp/tri3b_ali exp/dubm3b exp/tri3b_denlats \
  exp/tri3b_fmmi_c || exit 1;

for iter in 3 4 5 6 7 8; do
 steps/decode_fmmi.sh --nj $njobs --config conf/decode.config --cmd "$decode_cmd" --iter $iter \
   --transform-dir exp/tri3b/decode  exp/tri3b/graph data/test exp/tri3b_fmmi_c/decode_it$iter &
done

# for indirect one, use twice the learning rate.
steps/train_mmi_fmmi_indirect.sh --learning-rate 0.002 --schedule "fmmi fmmi fmmi fmmi mmi mmi mmi mmi" \
  --boost 0.1 --cmd "$train_cmd" data/train data/lang exp/tri3b_ali exp/dubm3b exp/tri3b_denlats \
  exp/tri3b_fmmi_d || exit 1;

for iter in 3 4 5 6 7 8; do
 steps/decode_fmmi.sh --nj $njobs --config conf/decode.config --cmd "$decode_cmd" --iter $iter \
   --transform-dir exp/tri3b/decode  exp/tri3b/graph data/test exp/tri3b_fmmi_d/decode_it$iter &
done

local/run_sgmm2.sh --nj $njobs

 echo
 echo "===== run.sh script is finished ====="
 echo
