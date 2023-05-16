<template>
  <div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      options: [{
          value: '0',
          label: '办学质量评估'
        }, {
          value: '1',
          label: '文献布尔检索'
      }],
      count: 0
    }
  },
  methods: {
    load () {
      this.count += 2
    }
  }
}
</script>

<style lang="less" scoped>

</style>